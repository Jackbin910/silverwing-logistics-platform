package com.silverwing.ai.config;

import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.router.DecisionModelChatModelRouter;
import dev.langchain4j.model.chat.router.RoutingStreamingChatModel;
import dev.langchain4j.model.decision.DecisionModel;
import dev.langchain4j.model.ollama.OllamaDecisionModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * LangChain4j 配置类
 * ChatModel 和 EmbeddingModel 均由 langchain4j-ollama-spring-boot-starter 自动配置
 * EmbeddingModel 通过 Nacos 属性 langchain4j.ollama.embedding-model.* 配置
 * 此处配置 PGVector 向量存储与 Ollama 流式对话模型；
 * 流式模型可选启用决策模型路由，由小决策模型判定难度、大小模型负责生成回答。
 */
@Slf4j
@Configuration
public class LangChain4jConfig {

    /** 决策模型常驻显存，避免每次请求重新加载（负值表示不自动卸载）。 */
    private static final int DECISION_KEEP_ALIVE = -1;

    /** 决策模型位于同步热路径，只重试一次，避免拖慢主链路。 */
    private static final int DECISION_MAX_RETRIES = 1;

    @Value("${langchain4j.vector-store.pgvector.host:localhost}")
    private String pgHost;

    @Value("${langchain4j.vector-store.pgvector.port:5432}")
    private Integer pgPort;

    @Value("${langchain4j.vector-store.pgvector.database:silverwing_vector}")
    private String pgDatabase;

    @Value("${langchain4j.vector-store.pgvector.user:silverwing}")
    private String pgUser;

    @Value("${langchain4j.vector-store.pgvector.password:silverwing_password}")
    private String pgPassword;

    @Value("${langchain4j.vector-store.pgvector.table:silverwing_embedding}")
    private String pgTable;

    @Value("${langchain4j.vector-store.pgvector.dimension:1024}")
    private Integer pgDimension;

    @Value("${langchain4j.vector-store.pgvector.create-table:true}")
    private Boolean pgCreateTable;

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private String redisPort;

    // ===== 小决策模型路由（默认关闭）=====
    // 开启后由 Ollama 决策模型判定请求难度，选择快速小模型或主模型生成回答；
    // 决策模型只输出分类与概率、不生成文本，低置信度或调用失败统一兜底到主模型。

    @Value("${ai.decision-routing.enabled:false}")
    private Boolean decisionRoutingEnabled;

    @Value("${ai.decision-routing.decision-model-name:nimble}")
    private String decisionModelName;

    @Value("${ai.decision-routing.fast-model-name:qwen2.5:1.5b-instruct}")
    private String fastModelName;

    @Value("${ai.decision-routing.decision-timeout-seconds:5}")
    private Integer decisionTimeoutSeconds;

    @Value("${ai.decision-routing.min-probability:0.7}")
    private Double minProbability;

    @Value("${ai.decision-routing.fast-route-description:寒暄、简短事实查询、简单状态确认}")
    private String fastRouteDescription;

    @Value("${ai.decision-routing.strong-route-description:业务查询、数据分析、多步推理、工具调用、长文生成}")
    private String strongRouteDescription;

    /**
     * PGVector 向量存储
     * 用于存储和检索知识库的文本向量
     * 连接独立的 PostgreSQL + pgvector 实例
     *
     * @return EmbeddingStore 实例
     */
    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        return PgVectorEmbeddingStore.builder()
                .host(pgHost)
                .port(pgPort)
                .database(pgDatabase)
                .user(pgUser)
                .password(pgPassword)
                .table(pgTable)
                .dimension(pgDimension)
                .useIndex(true)
                .indexListSize(100)
                .createTable(pgCreateTable)
                .dropTableFirst(false)
                .build();
    }

    /**
     * 流式对话模型
     * 复用与 ChatModel 相同的 Ollama 地址与模型名，用于知识库问答等场景的逐 token 流式输出
     * 仅当 Nacos 未配置 langchain4j.ollama.streaming-chat-model 时由本方法提供，避免重复 Bean
     * 开启决策路由后由小决策模型判定难度、大小模型负责生成回答，业务侧注入类型不变。
     * 注意：handler 形式的 chat() 会在调用线程同步完成路由判定，若 chat/stream 运行在
     * Reactor 事件循环线程上，开启后需评估阻塞影响。
     *
     * @param baseUrl     Ollama 服务地址
     * @param modelName   主模型名称
     * @param temperature 生成温度
     * @return StreamingChatModel 实例
     */
    @Bean
    public StreamingChatModel streamingChatModel(
            @Value("${langchain4j.ollama.chat-model.base-url}") String baseUrl,
            @Value("${langchain4j.ollama.chat-model.model-name}") String modelName,
            @Value("${langchain4j.ollama.chat-model.temperature}") Double temperature) {
        OllamaStreamingChatModel strongModel =
                buildOllamaStreamingModel(baseUrl, modelName, temperature);
        if (!Boolean.TRUE.equals(decisionRoutingEnabled)) {
            return strongModel;
        }

        // 决策模型只输出分类与概率，低置信度或调用失败时由 router 兜底到主模型
        OllamaStreamingChatModel fastModel =
                buildOllamaStreamingModel(baseUrl, fastModelName, temperature);
        DecisionModelChatModelRouter router = DecisionModelChatModelRouter.builder()
                .decisionModel(buildDecisionModel(baseUrl))
                .minProbability(minProbability)
                .build();
        log.info("决策模型路由已启用: decisionModel={}, fastModel={}, strongModel={}",
                decisionModelName, fastModelName, modelName);

        return RoutingStreamingChatModel.builder()
                .route("fast", fastRouteDescription, fastModel)
                .route("strong", strongRouteDescription, strongModel)
                .router(router)
                .defaultRoute("strong")
                .build();
    }

    /**
     * 构建 Ollama 流式聊天模型。
     *
     * @param baseUrl     Ollama 服务地址
     * @param modelName   模型名称
     * @param temperature 生成温度
     * @return 流式聊天模型
     */
    private OllamaStreamingChatModel buildOllamaStreamingModel(String baseUrl,
                                                                String modelName,
                                                                Double temperature) {
        return OllamaStreamingChatModel.builder()
                .baseUrl(baseUrl)
                .modelName(modelName)
                .temperature(temperature)
                .timeout(Duration.ofSeconds(300))
                .build();
    }

    /**
     * 构建 Ollama 小决策模型。
     * 决策模型位于同步热路径，需收敛超时与重试次数，避免拖慢整条 SSE 对话链路。
     *
     * @param baseUrl Ollama 服务地址
     * @return 决策模型实例
     */
    private DecisionModel buildDecisionModel(String baseUrl) {
        return OllamaDecisionModel.builder()
                .baseUrl(baseUrl)
                .modelName(decisionModelName)
                .timeout(Duration.ofSeconds(decisionTimeoutSeconds))
                .keepAlive(DECISION_KEEP_ALIVE)
                .maxRetries(DECISION_MAX_RETRIES)
                .build();
    }


    /**
     * Redis 聊天记忆存储
     * @return
     */
    @Bean
    public ChatMemoryStore chatMemoryStore() {
        return new RedisChatMemoryStore.Builder()
                .host(redisHost)
                .port(Integer.parseInt(redisPort))
                .build();
    }


}
