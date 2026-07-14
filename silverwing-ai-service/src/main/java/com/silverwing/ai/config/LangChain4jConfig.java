package com.silverwing.ai.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * LangChain4j 配置类
 * ChatModel 和 EmbeddingModel 均由 langchain4j-ollama-spring-boot-starter 自动配置
 * EmbeddingModel 通过 Nacos 属性 langchain4j.ollama.embedding-model.* 配置
 * 此处仅配置 PGVector 向量存储
 */
@Configuration
public class LangChain4jConfig {

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
     *
     * @param baseUrl     Ollama 服务地址
     * @param modelName   模型名称
     * @param temperature 生成温度
     * @return StreamingChatModel 实例
     */
    @Bean
    public StreamingChatModel streamingChatModel(
            @Value("${langchain4j.ollama.chat-model.base-url}") String baseUrl,
            @Value("${langchain4j.ollama.chat-model.model-name}") String modelName,
            @Value("${langchain4j.ollama.chat-model.temperature}") Double temperature) {
        return OllamaStreamingChatModel.builder()
                .baseUrl(baseUrl)
                .modelName(modelName)
                .temperature(temperature)
                .build();
    }
}
