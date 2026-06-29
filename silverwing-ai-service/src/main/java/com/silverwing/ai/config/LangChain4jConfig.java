package com.silverwing.ai.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.bgesmallzhv15q.BgeSmallZhV15QuantizedEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * LangChain4j 配置类
 * ChatModel 由 langchain4j-ollama-spring-boot-starter 自动配置
 * 此处配置本地 Embedding 模型和 PGVector 向量存储
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

    @Value("${langchain4j.vector-store.pgvector.dimension:512}")
    private Integer pgDimension;

    @Value("${langchain4j.vector-store.pgvector.create-table:true}")
    private Boolean pgCreateTable;

    /**
     * 本地 Embedding 模型（bge-small-zh-v15 量化版）
     * 用于 RAG 知识库向量化，无需联网，专为中文语义优化
     * 输出向量维度：512
     * 使用 @Primary 覆盖 starter 可能自动创建的 Ollama EmbeddingModel
     *
     * @return EmbeddingModel 实例
     */
    @Bean
    @Primary
    public EmbeddingModel embeddingModel() {
        return new BgeSmallZhV15QuantizedEmbeddingModel();
    }

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
}
