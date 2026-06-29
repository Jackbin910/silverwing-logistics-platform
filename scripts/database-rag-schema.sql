-- Database RAG 表结构定义
-- 用于存储数据库表结构元信息

CREATE TABLE IF NOT EXISTS `db_table_schema` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `database_name` VARCHAR(100) NOT NULL COMMENT '数据库名称',
    `table_name` VARCHAR(100) NOT NULL COMMENT '表名称',
    `table_comment` VARCHAR(500) COMMENT '表中文描述',
    `column_name` VARCHAR(100) NOT NULL COMMENT '列名称',
    `column_comment` VARCHAR(500) COMMENT '列中文描述',
    `data_type` VARCHAR(100) COMMENT '数据类型',
    `is_primary_key` VARCHAR(10) DEFAULT 'NO' COMMENT '是否主键: YES/NO',
    `is_nullable` VARCHAR(10) DEFAULT 'YES' COMMENT '是否可为空: YES/NO',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_table_name` (`table_name`),
    KEY `idx_database_table` (`database_name`, `table_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据库表结构定义';

-- 初始化订单和订单明细表的结构信息
INSERT INTO `db_table_schema` (`database_name`, `table_name`, `table_comment`, `column_name`, `column_comment`, `data_type`, `is_primary_key`, `is_nullable`)
VALUES
-- 订单表
('silverwing_logistics', 'logistics_order', '订单表', 'id', '订单ID', 'BIGINT', 'YES', 'NO'),
('silverwing_logistics', 'logistics_order', '订单表', 'order_no', '订单编号', 'VARCHAR(50)', 'NO', 'NO'),
('silverwing_logistics', 'logistics_order', '订单表', 'order_type', '订单类型: surgery_material-手术物资, bulk_material-批量物资, medicine-药品, sample-样本', 'VARCHAR(20)', 'NO', 'NO'),
('silverwing_logistics', 'logistics_order', '订单表', 'status', '订单状态: pending-待接单, in_progress-进行中, completed-已完成, cancelled-已取消', 'VARCHAR(20)', 'NO', 'NO'),
('silverwing_logistics', 'logistics_order', '订单表', 'department', '部门/科室', 'VARCHAR(100)', 'NO', 'YES'),
('silverwing_logistics', 'logistics_order', '订单表', 'target_location', '目标位置', 'VARCHAR(200)', 'NO', 'NO'),
('silverwing_logistics', 'logistics_order', '订单表', 'contact_name', '联系人', 'VARCHAR(50)', 'NO', 'NO'),
('silverwing_logistics', 'logistics_order', '订单表', 'contact_phone', '联系电话', 'VARCHAR(20)', 'NO', 'NO'),
('silverwing_logistics', 'logistics_order', '订单表', 'delivery_type', '配送方式: robot_dog-机器狗, robot-机器人, agv-AGV, manual-人工', 'VARCHAR(20)', 'NO', 'YES'),
('silverwing_logistics', 'logistics_order', '订单表', 'device_id', '配送设备ID', 'VARCHAR(50)', 'NO', 'YES'),
('silverwing_logistics', 'logistics_order', '订单表', 'urgent', '是否紧急: 0-否, 1-是', 'TINYINT', 'NO', 'YES'),
('silverwing_logistics', 'logistics_order', '订单表', 'remark', '备注', 'TEXT', 'NO', 'YES'),
('silverwing_logistics', 'logistics_order', '订单表', 'expected_arrival_time', '期望送达时间', 'DATETIME', 'NO', 'YES'),
('silverwing_logistics', 'logistics_order', '订单表', 'actual_arrival_time', '实际送达时间', 'DATETIME', 'NO', 'YES'),
('silverwing_logistics', 'logistics_order', '订单表', 'create_time', '创建时间', 'DATETIME', 'NO', 'YES'),
-- 订单明细表
('silverwing_logistics', 'logistics_order_item', '订单明细表', 'id', '明细ID', 'BIGINT', 'YES', 'NO'),
('silverwing_logistics', 'logistics_order_item', '订单明细表', 'order_id', '订单ID (关联logistics_order.id)', 'BIGINT', 'NO', 'NO'),
('silverwing_logistics', 'logistics_order_item', '订单明细表', 'item_code', '物品编码', 'VARCHAR(50)', 'NO', 'NO'),
('silverwing_logistics', 'logistics_order_item', '订单明细表', 'item_name', '物品名称', 'VARCHAR(100)', 'NO', 'NO'),
('silverwing_logistics', 'logistics_order_item', '订单明细表', 'specification', '规格', 'VARCHAR(200)', 'NO', 'YES'),
('silverwing_logistics', 'logistics_order_item', '订单明细表', 'quantity', '数量', 'INT', 'NO', 'NO'),
('silverwing_logistics', 'logistics_order_item', '订单明细表', 'unit', '单位', 'VARCHAR(20)', 'NO', 'YES'),
('silverwing_logistics', 'logistics_order_item', '订单明细表', 'create_time', '创建时间', 'DATETIME', 'NO', 'YES')
ON DUPLICATE KEY UPDATE column_comment = VALUES(column_comment);
