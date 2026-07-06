-- ========================================
-- PGVector 初始化脚本
-- ========================================
-- 使用方法：
--   psql -U silverwing -d silverwing_vector -f init-pgvector.sql
--
-- 前提条件：
--   1. PostgreSQL 14+ 已安装
--   2. pgvector 扩展已安装（参考下方安装说明）
-- ========================================

-- 启用 pgvector 扩展
CREATE EXTENSION IF NOT EXISTS vector;

-- 验证扩展是否创建成功
SELECT extname, extversion FROM pg_extension WHERE extname = 'vector';

-- ========================================
-- 向量表：存储文本嵌入向量（LangChain4j PGVectorEmbeddingStore 标准表结构）
-- ========================================
-- 注意：如果使用 langchain4j 的 createTable=true 配置，此表会自动创建
-- 此脚本用于手动初始化或验证

CREATE TABLE IF NOT EXISTS silverwing_embedding (
    id UUID PRIMARY KEY,
    embedding vector(512) NOT NULL,
    text TEXT NOT NULL,
    metadata JSONB
);

-- 创建 IVFFlat 索引（适合中等规模数据，建议在数据量 > 1000 条后创建）
-- IVFFlat 需要先有一定数据量来训练聚类中心
-- CREATE INDEX IF NOT EXISTS idx_silverwing_embedding_ivfflat
--     ON silverwing_embedding USING ivfflat (embedding vector_cosine_ops)
--     WITH (lists = 100);

-- 创建 HNSW 索引（适合大规模数据，无需训练步骤，查询精度更高）
CREATE INDEX IF NOT EXISTS idx_silverwing_embedding_hnsw
    ON silverwing_embedding USING hnsw (embedding vector_cosine_ops)
    WITH (m = 16, ef_construction = 64);

-- ========================================
-- 知识库管理表（用于管理 RAG 知识文档的元信息）
-- ========================================
CREATE TABLE IF NOT EXISTS knowledge_document (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL ,
    file_name VARCHAR(500),
    file_type VARCHAR(20),
    file_size BIGINT,
    source_url VARCHAR(500),
    status SMALLINT DEFAULT 1 ,
    chunk_count INT DEFAULT 0 ,
    description TEXT ,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE knowledge_document IS '知识库文档主表';

-- 字段注释（PG标准写法）
COMMENT ON COLUMN knowledge_document.title IS '文档标题';
COMMENT ON COLUMN knowledge_document.file_name IS '原始文件名';
COMMENT ON COLUMN knowledge_document.file_type IS '文件类型（如：pdf、docx、md）';
COMMENT ON COLUMN knowledge_document.file_size IS '文件大小（字节）';
COMMENT ON COLUMN knowledge_document.source_url IS '来源地址';
COMMENT ON COLUMN knowledge_document.status IS '状态（0-禁用 1-启用）';
COMMENT ON COLUMN knowledge_document.chunk_count IS '分片数量';
COMMENT ON COLUMN knowledge_document.description IS '文档描述';
COMMENT ON COLUMN knowledge_document.created_at IS '创建时间';
COMMENT ON COLUMN knowledge_document.updated_at IS '更新时间';

-- 知识库文档索引
CREATE INDEX IF NOT EXISTS idx_knowledge_doc_status ON knowledge_document(status);



