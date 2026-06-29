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
    title VARCHAR(255) NOT NULL COMMENT '文档标题',
    category VARCHAR(100) COMMENT '文档分类（如：设备手册、FAQ、维护记录）',
    source_type VARCHAR(50) COMMENT '来源类型（如：pdf、word、web、manual）',
    source_url VARCHAR(500) COMMENT '来源地址',
    warehouse_id VARCHAR(50) COMMENT '仓库 ID（用于按仓库隔离知识）',
    device_type VARCHAR(100) COMMENT '设备类型（用于按设备类型过滤）',
    status SMALLINT DEFAULT 1 COMMENT '状态（0-禁用 1-启用）',
    chunk_count INT DEFAULT 0 COMMENT '分片数量',
    file_size BIGINT COMMENT '文件大小（字节）',
    description TEXT COMMENT '文档描述',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 知识库文档索引
CREATE INDEX IF NOT EXISTS idx_knowledge_doc_category ON knowledge_document(category);
CREATE INDEX IF NOT EXISTS idx_knowledge_doc_warehouse ON knowledge_document(warehouse_id);
CREATE INDEX IF NOT EXISTS idx_knowledge_doc_device ON knowledge_document(device_type);
CREATE INDEX IF NOT EXISTS idx_knowledge_doc_status ON knowledge_document(status);

-- ========================================
-- 聊天记录表（用于持久化 AI 对话历史）
-- ========================================
CREATE TABLE IF NOT EXISTS ai_chat_history (
    id BIGSERIAL PRIMARY KEY,
    session_id VARCHAR(64) NOT NULL COMMENT '会话 ID',
    user_id VARCHAR(64) COMMENT '用户 ID',
    role VARCHAR(20) NOT NULL COMMENT '角色（user / assistant / system）',
    content TEXT NOT NULL COMMENT '消息内容',
    token_count INT DEFAULT 0 COMMENT 'Token 数量',
    model_name VARCHAR(50) COMMENT '使用的模型名称',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_ai_chat_session ON ai_chat_history(session_id);
CREATE INDEX IF NOT EXISTS idx_ai_chat_user ON ai_chat_history(user_id);

-- ========================================
-- pgvector 安装说明（Docker 部署）
-- ========================================
--
-- 方式一：使用 pgvector 官方镜像
--   docker run -d \
--     --name pgvector \
--     -e POSTGRES_USER=silverwing \
--     -e POSTGRES_PASSWORD=silverwing_password \
--     -e POSTGRES_DB=silverwing_vector \
--     -p 5432:5432 \
--     pgvector/pgvector:pg16
--
-- 方式二：使用自定义 Dockerfile
--   FROM postgres:16-alpine
--   RUN apt-get update && apt-get install -y \
--       postgresql-16-pgvector \
--     && rm -rf /var/lib/apt/lists/*
--
-- 方式三：在已有 PostgreSQL 上安装扩展
--   Ubuntu/Debian: sudo apt-get install postgresql-16-pgvector
--   CentOS/RHEL:   sudo yum install pgvector_16
--   macOS:         brew install pgvector
-- ========================================
