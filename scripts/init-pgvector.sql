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
-- 向量表：存储文本嵌入向量
-- 列名与 LangChain4j PgVectorEmbeddingStore 1.17.1 默认表结构保持一致
-- ========================================
-- 注意：
--   1. 如果使用 langchain4j 的 createTable=true 配置，此表会自动创建（用默认列名 embedding_id）
--      此脚本用于手动初始化或验证，列名必须与 LangChain4j 默认一致，否则写入时报
--      "column embedding_id of relation silverwing_embedding does not exist"
--   2. 知识库文档元信息（标题、文件名、状态等）存储在 MySQL 的 ai_knowledge_document 表，
--      由 KnowledgeDocumentMapper（MyBatis-Plus）管理。PG 只存储向量数据。
--      两库通过 metadata JSONB 中的 documentId 字段在应用层建立逻辑关联，无需物理外键。

CREATE TABLE IF NOT EXISTS silverwing_embedding (
    embedding_id UUID PRIMARY KEY,        -- LangChain4j 默认 ID 列名，不可改名
    embedding vector(1024) NOT NULL,
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



