-- ============================================================
-- AI服务数据库脚本
-- 创建时间: 2026-03-30
-- 说明: 知识库管理、AI对话记录等
-- ============================================================

-- 创建AI服务专用数据库
CREATE DATABASE IF NOT EXISTS silverwing_ai DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE silverwing_ai;

-- ============================================================
-- 知识库文档表
-- ============================================================
DROP TABLE IF EXISTS ai_knowledge_document;

CREATE TABLE ai_knowledge_document (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    document_id VARCHAR(64) NOT NULL UNIQUE COMMENT '文档唯一标识(UUID)',
    title VARCHAR(500) NOT NULL COMMENT '文档标题',
    category VARCHAR(100) COMMENT '文档分类（设备手册、FAQ、维护记录等）',
    source_type VARCHAR(50) COMMENT '来源类型（manual、web、faq）',
    warehouse_id VARCHAR(64) COMMENT '仓库ID（用于知识隔离）',
    device_type VARCHAR(100) COMMENT '设备类型',
    word_count INT DEFAULT 0 COMMENT '文档字数',
    chunk_count INT DEFAULT 0 COMMENT '导入的分片数量',
    status TINYINT DEFAULT 0 COMMENT '文档状态（0-待处理、1-已导入、2-导入失败）',
    error_msg TEXT COMMENT '错误信息',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '是否删除（0-否、1-是）',
    INDEX idx_document_id (document_id),
    INDEX idx_category (category),
    INDEX idx_warehouse_id (warehouse_id),
    INDEX idx_device_type (device_type),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识库文档表';

-- ============================================================
-- AI对话记录表
-- ============================================================
DROP TABLE IF EXISTS ai_conversation;

CREATE TABLE ai_conversation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    conversation_id VARCHAR(64) NOT NULL COMMENT '对话会话ID',
    user_id BIGINT COMMENT '用户ID',
    session_type VARCHAR(50) COMMENT '会话类型（order_query、device_query、knowledge_qa）',
    user_message TEXT NOT NULL COMMENT '用户消息',
    ai_message TEXT COMMENT 'AI回复消息',
    intent VARCHAR(100) COMMENT '识别的意图',
    entities JSON COMMENT '提取的实体（JSON格式）',
    context JSON COMMENT '对话上下文',
    token_count INT DEFAULT 0 COMMENT '使用的token数量',
    response_time INT DEFAULT 0 COMMENT '响应时间（毫秒）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_conversation_id (conversation_id),
    INDEX idx_user_id (user_id),
    INDEX idx_session_type (session_type),
    INDEX idx_intent (intent),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI对话记录表';

-- ============================================================
-- 意图配置表
-- ============================================================
DROP TABLE IF EXISTS ai_intent_config;

CREATE TABLE ai_intent_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    intent_code VARCHAR(100) NOT NULL UNIQUE COMMENT '意图编码',
    intent_name VARCHAR(100) NOT NULL COMMENT '意图名称',
    description VARCHAR(500) COMMENT '意图描述',
    handler_class VARCHAR(255) COMMENT '处理器类名',
    keywords JSON COMMENT '关键词列表（用于规则匹配）',
    examples TEXT COMMENT '示例语句',
    priority INT DEFAULT 0 COMMENT '优先级（越大越高）',
    enabled TINYINT DEFAULT 1 COMMENT '是否启用（0-禁用、1-启用）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_intent_code (intent_code),
    INDEX idx_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='意图配置表';

-- 初始化预设意图数据
INSERT INTO ai_intent_config (intent_code, intent_name, description, priority, enabled) VALUES
('order_query', '订单查询', '查询订单状态、配送进度等信息', 10, 1),
('device_query', '设备查询', '查询设备位置、状态等信息', 10, 1),
('device_status', '设备状态', '获取设备运行状态、故障信息', 10, 1),
('fault_report', '故障报修', '设备故障报修相关', 10, 1),
('knowledge_qa', '知识问答', '知识库相关问答', 5, 1),
('order_create', '创建订单', '创建配送订单', 10, 1),
('workorder_create', '创建工单', '创建维护工单', 10, 1);

-- ============================================================
-- 实体类型配置表
-- ============================================================
DROP TABLE IF EXISTS ai_entity_type;

CREATE TABLE ai_entity_type (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    entity_type VARCHAR(100) NOT NULL UNIQUE COMMENT '实体类型编码',
    entity_name VARCHAR(100) NOT NULL COMMENT '实体类型名称',
    description VARCHAR(500) COMMENT '实体类型描述',
    examples TEXT COMMENT '示例实体',
    enabled TINYINT DEFAULT 1 COMMENT '是否启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='实体类型配置表';

-- 初始化预设实体类型
INSERT INTO ai_entity_type (entity_type, entity_name, description, examples) VALUES
('ORDER_NO', '订单号', '配送订单编号', 'ORD20240315001, ORD-20240315-001'),
('DEVICE_CODE', '设备编码', '物流设备唯一标识', 'DEV001, PVT-001'),
('DEVICE_TYPE', '设备类型', '设备类型名称', '气动物流、机器人、仓储'),
('LOCATION', '位置', '物理位置信息', '3楼手术室、门诊药房'),
('TIME', '时间', '时间相关', '今天、明天、上午9点'),
('DEPARTMENT', '科室', '医院科室', '手术室、药房、检验科'),
('ITEM_NAME', '物品名称', '物资名称', '输液器、注射器、药品');

-- ============================================================
-- 设备运行日志表（用于AI分析）
-- ============================================================
DROP TABLE IF EXISTS ai_device_log;

CREATE TABLE ai_device_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    device_id VARCHAR(64) NOT NULL COMMENT '设备ID',
    device_code VARCHAR(64) COMMENT '设备编码',
    device_type VARCHAR(50) COMMENT '设备类型',
    log_type VARCHAR(50) COMMENT '日志类型（error、warning、info）',
    log_content TEXT NOT NULL COMMENT '日志内容',
    metric_data JSON COMMENT '指标数据（如温度、压力等）',
    warehouse_id VARCHAR(64) COMMENT '仓库ID',
    location VARCHAR(200) COMMENT '设备位置',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '日志时间',
    INDEX idx_device_id (device_id),
    INDEX idx_device_type (device_type),
    INDEX idx_log_type (log_type),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备运行日志表';
