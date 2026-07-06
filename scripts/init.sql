-- 银翼智驭医流综合管理平台 - 数据库初始化脚本

-- 创建数据库
CREATE DATABASE IF NOT EXISTS silverwing_logistics DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE silverwing_logistics;

-- ========================================
-- 用户权限表
-- ========================================
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码',
    `nickname` VARCHAR(50) COMMENT '昵称',
    `avatar` VARCHAR(255) COMMENT '头像',
    `phone` VARCHAR(20) COMMENT '手机号',
    `email` VARCHAR(100) COMMENT '邮箱',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标记',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ========================================
-- 角色表
-- ========================================
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    `role_code` VARCHAR(50) NOT NULL COMMENT '角色编码（如：ADMIN、USER）',
    `role_name` VARCHAR(100) NOT NULL COMMENT '角色名称（如：管理员、普通用户）',
    `description` VARCHAR(255) COMMENT '角色描述',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标记',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- ========================================
-- 用户角色关联表
-- ========================================
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- ========================================
-- 订单表
-- ========================================
DROP TABLE IF EXISTS `logistics_order`;
CREATE TABLE `logistics_order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    `order_no` VARCHAR(50) NOT NULL COMMENT '订单编号',
    `order_type` VARCHAR(20) NOT NULL COMMENT '订单类型: surgery_material-手术物资, bulk_material-批量物资, medicine-药品, sample-样本',
    `status` VARCHAR(20) NOT NULL COMMENT '订单状态: pending-待接单, in_progress-进行中, completed-已完成, cancelled-已取消',
    `department` VARCHAR(100) COMMENT '部门/科室',
    `target_location` VARCHAR(200) NOT NULL COMMENT '目标位置',
    `contact_name` VARCHAR(50) NOT NULL COMMENT '联系人',
    `contact_phone` VARCHAR(20) NOT NULL COMMENT '联系电话',
    `delivery_type` VARCHAR(20) COMMENT '配送方式: robot_dog-机器狗, robot-机器人, agv-AGV, manual-人工',
    `device_id` VARCHAR(50) COMMENT '配送设备ID',
    `urgent` TINYINT DEFAULT 0 COMMENT '是否紧急: 0-否, 1-是',
    `remark` TEXT COMMENT '备注',
    `expected_arrival_time` DATETIME COMMENT '期望送达时间',
    `actual_arrival_time` DATETIME COMMENT '实际送达时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标记',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_order_type` (`order_type`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- ========================================
-- 订单明细表
-- ========================================
DROP TABLE IF EXISTS `logistics_order_item`;
CREATE TABLE `logistics_order_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `item_code` VARCHAR(50) NOT NULL COMMENT '物品编码',
    `item_name` VARCHAR(100) NOT NULL COMMENT '物品名称',
    `specification` VARCHAR(200) COMMENT '规格',
    `quantity` INT NOT NULL COMMENT '数量',
    `unit` VARCHAR(20) COMMENT '单位',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';

-- ========================================
-- 订单追踪记录表
-- ========================================
DROP TABLE IF EXISTS `logistics_order_track`;
CREATE TABLE `logistics_order_track` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `event` VARCHAR(100) NOT NULL COMMENT '事件',
    `location` VARCHAR(200) COMMENT '位置',
    `description` VARCHAR(500) COMMENT '描述',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单追踪记录表';

-- ========================================
-- 设备表
-- ========================================
DROP TABLE IF EXISTS `logistics_device`;
CREATE TABLE `logistics_device` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '设备ID',
    `device_code` VARCHAR(50) NOT NULL COMMENT '设备编码',
    `device_name` VARCHAR(100) NOT NULL COMMENT '设备名称',
    `device_type` VARCHAR(20) NOT NULL COMMENT '设备类型: robot_dog-机器狗, robot-机器人, agv-AGV, pneumatic-气动物流, station-传输站点, warehouse-智能仓储',
    `status` VARCHAR(20) NOT NULL COMMENT '设备状态: normal-正常, fault-故障, maintenance-维护中',
    `location` VARCHAR(200) COMMENT '当前位置',
    `battery_level` INT COMMENT '电量百分比',
    `last_active_time` DATETIME COMMENT '最后活跃时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标记',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_device_code` (`device_code`),
    KEY `idx_device_type` (`device_type`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备表';

-- ========================================
-- 设备运行数据表
-- ========================================
DROP TABLE IF EXISTS `logistics_device_runtime`;
CREATE TABLE `logistics_device_runtime` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `device_id` BIGINT NOT NULL COMMENT '设备ID',
    `metric_name` VARCHAR(50) NOT NULL COMMENT '指标名称',
    `metric_value` DECIMAL(18,4) COMMENT '指标值',
    `unit` VARCHAR(20) COMMENT '单位',
    `record_time` DATETIME NOT NULL COMMENT '记录时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_device_id` (`device_id`),
    KEY `idx_metric_name` (`metric_name`),
    KEY `idx_record_time` (`record_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备运行数据表';

-- ========================================
-- 工单表
-- ========================================
DROP TABLE IF EXISTS `logistics_work_order`;
CREATE TABLE `logistics_work_order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '工单ID',
    `work_order_no` VARCHAR(50) NOT NULL COMMENT '工单编号',
    `work_order_type` VARCHAR(20) NOT NULL COMMENT '工单类型: maintenance-维护, repair-维修, inspection-巡检',
    `device_id` BIGINT COMMENT '设备ID',
    `device_code` VARCHAR(50) COMMENT '设备编码',
    `title` VARCHAR(200) NOT NULL COMMENT '标题',
    `description` TEXT COMMENT '描述',
    `status` VARCHAR(20) NOT NULL COMMENT '状态: pending-待处理, processing-处理中, completed-已完成',
    `priority` VARCHAR(20) COMMENT '优先级: low-低, medium-中, high-高, urgent-紧急',
    `handler_id` BIGINT COMMENT '处理人ID',
    `handler_name` VARCHAR(50) COMMENT '处理人姓名',
    `expected_time` DATETIME COMMENT '期望完成时间',
    `actual_time` DATETIME COMMENT '实际完成时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标记',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_work_order_no` (`work_order_no`),
    KEY `idx_device_id` (`device_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工单表';

-- ========================================
-- 插入初始数据
-- ========================================

-- 插入默认用户
-- 注意：以下密码哈希需要使用 BCrypt 工具类生成
-- 推荐使用：BCrypt.hashpw("admin123", BCrypt.gensalt()) 生成 admin 密码
-- 推荐使用：BCrypt.hashpw("nurse123", BCrypt.gensalt()) 生成 nurse1 密码
-- 临时测试：可以先用明文密码 "123456" 的 BCrypt 哈希
INSERT INTO `sys_user` (`username`, `password`, `nickname`, `phone`, `email`, `status`) VALUES
('admin', '$2a$10$JjUIlZViZv2oZk.ANIdyaero4iEL2FwJ1xPRCHrMEo3l054WFebmi', '系统管理员', '13800138000', 'admin@silverwing.com', 1),
('nurse1', '$2a$10$BncrHbpJ703Xy0IqohRVCOKnE/7cXdIUYnY1P3EQUwA8wDi2Me5E2', '护士-手术室1', '13800138001', 'nurse1@hospital.com', 1);

-- 插入默认角色
INSERT INTO `sys_role` (`role_code`, `role_name`, `description`, `status`) VALUES
('ADMIN', '系统管理员', '拥有系统所有权限', 1),
('USER', '普通用户', '基础业务权限', 1),
('OPERATOR', '运营人员', '运营管理权限', 1);

-- 插入用户角色关联（admin 拥有 ADMIN 角色，nurse1 拥有 USER 角色）
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES
(1, 1),  -- admin -> ADMIN
(2, 2);  -- nurse1 -> USER

-- 插入测试设备
INSERT INTO `logistics_device` (`device_code`, `device_name`, `device_type`, `status`, `location`, `battery_level`) VALUES
('RD001', '机器狗-01', 'robot_dog', 'normal', '一楼大厅', 85),
('RD002', '机器狗-02', 'robot_dog', 'normal', '二楼走廊', 72),
('RB001', '配送机器人-01', 'robot', 'normal', '仓储区', 90),
('RB002', '配送机器人-02', 'robot', 'maintenance', '维修区', 50),
('AGV001', 'AGV-01', 'agv', 'normal', '仓库A区', 95),
('ST001', '气动物流站点-01', 'station', 'fault', '住院部3楼', NULL);

-- ========================================
-- Nacos 配置中心数据库
-- ========================================
CREATE DATABASE IF NOT EXISTS nacos_config DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建 Nacos 专用用户（与 onepanel-infra.env 中的 NACOS_SERVICE_USER/NACOS_SERVICE_PASSWORD 对应）
-- 注意：MySQL 8.0 需要在用户存在时跳过创建，用 ON DUPLICATE KEY 的逻辑
DROP USER IF EXISTS 'nacos'@'%';
CREATE USER 'nacos'@'%' IDENTIFIED BY '123456';
GRANT ALL PRIVILEGES ON nacos_config.* TO 'nacos'@'%';
FLUSH PRIVILEGES;

-- 切换到 nacos_config 数据库，执行 Nacos 内置表结构
-- 注意：MySQL 8.0 不支持 anonymous row format 的默认值，部分列必须显式指定
USE nacos_config;

-- ========================================
-- Nacos 2.4.x 配置中心表结构
-- 来源：nacos 2.4.3 distribution/conf/mysql-schema.sql
-- ========================================

-- 配置信息表
DROP TABLE IF EXISTS `config_info`;
CREATE TABLE `config_info` (
    `id` BIGINT(64) NOT NULL AUTO_INCREMENT COMMENT 'id',
    `data_id` VARCHAR(255) NOT NULL COMMENT 'data_id',
    `group_id` VARCHAR(128) DEFAULT NULL COMMENT 'group_id',
    `content` LONGTEXT NOT NULL COMMENT 'content',
    `md5` VARCHAR(32) DEFAULT NULL COMMENT 'md5',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
    `src_user` TEXT COMMENT 'source user',
    `src_ip` VARCHAR(50) DEFAULT NULL COMMENT 'source ip',
    `app_name` VARCHAR(128) DEFAULT NULL COMMENT 'app_name',
    `tenant_id` VARCHAR(128) DEFAULT '' COMMENT '租户字段',
    `c_desc` VARCHAR(256) DEFAULT NULL COMMENT 'configuration description',
    `c_use` VARCHAR(64) DEFAULT NULL COMMENT 'configuration usage',
    `effect` VARCHAR(64) DEFAULT NULL COMMENT '配置生效的描述',
    `type` VARCHAR(64) DEFAULT NULL COMMENT '配置的类型',
    `c_schema` TEXT DEFAULT NULL COMMENT '配置的模式',
    `encrypted_data_key` VARCHAR(1024) DEFAULT '' COMMENT '加密密钥',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_configinfo_datagrouptenant` (`data_id`, `group_id`, `tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='config_info';

-- 配置信息聚合表
DROP TABLE IF EXISTS `config_info_aggr`;
CREATE TABLE `config_info_aggr` (
    `id` BIGINT(64) NOT NULL AUTO_INCREMENT COMMENT 'id',
    `data_id` VARCHAR(255) NOT NULL COMMENT 'data_id',
    `group_id` VARCHAR(128) NOT NULL COMMENT 'group_id',
    `datum_id` VARCHAR(255) NOT NULL COMMENT 'datum_id',
    `content` LONGTEXT NOT NULL COMMENT '内容',
    `gmt_modified` DATETIME NOT NULL COMMENT '修改时间',
    `app_name` VARCHAR(128) DEFAULT NULL COMMENT 'app_name',
    `tenant_id` VARCHAR(128) DEFAULT '' COMMENT '租户字段',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_configinfoaggr_datagrouptenantdatum` (`data_id`, `group_id`, `tenant_id`, `datum_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='增加租户字段';

-- 配置信息灰度表
DROP TABLE IF EXISTS `config_info_beta`;
CREATE TABLE `config_info_beta` (
    `id` BIGINT(64) NOT NULL AUTO_INCREMENT COMMENT 'id',
    `data_id` VARCHAR(255) NOT NULL COMMENT 'data_id',
    `group_id` VARCHAR(128) NOT NULL COMMENT 'group_id',
    `app_name` VARCHAR(128) DEFAULT NULL COMMENT 'app_name',
    `content` LONGTEXT NOT NULL COMMENT 'content',
    `beta_ips` VARCHAR(1024) DEFAULT NULL COMMENT 'betaIps',
    `md5` VARCHAR(32) DEFAULT NULL COMMENT 'md5',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
    `src_user` TEXT COMMENT 'source user',
    `src_ip` VARCHAR(50) DEFAULT NULL COMMENT 'source ip',
    `tenant_id` VARCHAR(128) DEFAULT '' COMMENT '租户字段',
    `encrypted_data_key` VARCHAR(1024) DEFAULT '' COMMENT '加密密钥',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_configinfobeta_datagrouptenant` (`data_id`, `group_id`, `tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='config_info_beta';

-- 配置信息标签表
DROP TABLE IF EXISTS `config_info_tag`;
CREATE TABLE `config_info_tag` (
    `id` BIGINT(64) NOT NULL AUTO_INCREMENT COMMENT 'id',
    `data_id` VARCHAR(255) NOT NULL COMMENT 'data_id',
    `group_id` VARCHAR(128) NOT NULL COMMENT 'group_id',
    `tenant_id` VARCHAR(128) DEFAULT '' COMMENT 'tenant_id',
    `tag_id` VARCHAR(128) NOT NULL COMMENT 'tag_id',
    `app_name` VARCHAR(128) DEFAULT NULL COMMENT 'app_name',
    `content` LONGTEXT NOT NULL COMMENT 'content',
    `md5` VARCHAR(32) DEFAULT NULL COMMENT 'md5',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
    `src_user` TEXT COMMENT 'source user',
    `src_ip` VARCHAR(50) DEFAULT NULL COMMENT 'source ip',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_configinfotag_datagrouptenanttag` (`data_id`, `group_id`, `tenant_id`, `tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='config_info_tag';

-- 配置标签关系表
DROP TABLE IF EXISTS `config_tags_relation`;
CREATE TABLE `config_tags_relation` (
    `id` BIGINT(64) NOT NULL COMMENT 'id',
    `tag_name` VARCHAR(128) NOT NULL COMMENT 'tag_name',
    `tag_type` VARCHAR(64) DEFAULT NULL COMMENT 'tag_type',
    `data_id` VARCHAR(255) NOT NULL COMMENT 'data_id',
    `group_id` VARCHAR(128) NOT NULL COMMENT 'group_id',
    `tenant_id` VARCHAR(128) DEFAULT '' COMMENT 'tenant_id',
    `nid` BIGINT(64) NOT NULL AUTO_INCREMENT COMMENT 'nid, 自增长标识',
    PRIMARY KEY (`nid`),
    UNIQUE KEY `uk_configtagrelation_configidtag` (`id`, `tag_name`, `tag_type`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='config_tag_relation';

-- 分组容量表
DROP TABLE IF EXISTS `group_capacity`;
CREATE TABLE `group_capacity` (
    `id` BIGINT(64) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `group_id` VARCHAR(128) NOT NULL DEFAULT '' COMMENT 'Group ID，空字符表示整个集群',
    `quota` INT(10) UNSIGNED NOT NULL DEFAULT '0' COMMENT '配额，0表示使用默认值',
    `usage` INT(10) UNSIGNED NOT NULL DEFAULT '0' COMMENT '使用量',
    `max_size` INT(10) UNSIGNED NOT NULL DEFAULT '0' COMMENT '单个配置大小上限，单位为字节，0表示使用默认值',
    `max_aggr_count` INT(10) UNSIGNED NOT NULL DEFAULT '0' COMMENT '聚合子配置最大个数，0表示使用默认值',
    `max_aggr_size` INT(10) UNSIGNED NOT NULL DEFAULT '0' COMMENT '单个聚合数据的子配置大小上限，单位为字节，0表示使用默认值',
    `max_history_count` INT(10) UNSIGNED NOT NULL DEFAULT '0' COMMENT '最大变更历史数量',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_group_id` (`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='集群、各Group容量信息表';

-- 历史配置信息表
DROP TABLE IF EXISTS `his_config_info`;
CREATE TABLE `his_config_info` (
    `id` BIGINT(64) NOT NULL COMMENT 'id',
    `nid` BIGINT(64) NOT NULL AUTO_INCREMENT COMMENT 'nid, 自增长标识',
    `data_id` VARCHAR(255) NOT NULL COMMENT 'data_id',
    `group_id` VARCHAR(128) NOT NULL COMMENT 'group_id',
    `app_name` VARCHAR(128) DEFAULT NULL COMMENT 'app_name',
    `content` LONGTEXT NOT NULL COMMENT 'content',
    `md5` VARCHAR(32) DEFAULT NULL COMMENT 'md5',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
    `src_user` TEXT COMMENT 'source user',
    `src_ip` VARCHAR(50) DEFAULT NULL COMMENT 'source ip',
    `op_type` CHAR(10) DEFAULT NULL COMMENT '操作类型',
    `tenant_id` VARCHAR(128) DEFAULT '' COMMENT '租户字段',
    `encrypted_data_key` VARCHAR(1024) DEFAULT '' COMMENT '加密密钥',
    PRIMARY KEY (`nid`),
    KEY `idx_gmt_create` (`gmt_create`),
    KEY `idx_gmt_modified` (`gmt_modified`),
    KEY `idx_did` (`data_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='多租户改造';

-- 租户容量表
DROP TABLE IF EXISTS `tenant_capacity`;
CREATE TABLE `tenant_capacity` (
    `id` BIGINT(64) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `tenant_id` VARCHAR(128) NOT NULL DEFAULT '' COMMENT 'Tenant ID',
    `quota` INT(10) UNSIGNED NOT NULL DEFAULT '0' COMMENT '配额，0表示使用默认值',
    `usage` INT(10) UNSIGNED NOT NULL DEFAULT '0' COMMENT '使用量',
    `max_size` INT(10) UNSIGNED NOT NULL DEFAULT '0' COMMENT '单个配置大小上限，单位为字节，0表示使用默认值',
    `max_aggr_count` INT(10) UNSIGNED NOT NULL DEFAULT '0' COMMENT '聚合子配置最大个数',
    `max_aggr_size` INT(10) UNSIGNED NOT NULL DEFAULT '0' COMMENT '单个聚合数据的子配置大小上限，单位为字节，0表示使用默认值',
    `max_history_count` INT(10) UNSIGNED NOT NULL DEFAULT '0' COMMENT '最大变更历史数量',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='租户容量信息表';

-- 租户信息表
DROP TABLE IF EXISTS `tenant_info`;
CREATE TABLE `tenant_info` (
    `id` BIGINT(64) NOT NULL AUTO_INCREMENT COMMENT 'id',
    `kp` VARCHAR(128) NOT NULL COMMENT 'kp',
    `tenant_id` VARCHAR(128) DEFAULT '' COMMENT 'tenant_id',
    `tenant_name` VARCHAR(128) DEFAULT '' COMMENT 'tenant_name',
    `tenant_desc` VARCHAR(256) DEFAULT NULL COMMENT 'tenant_desc',
    `create_source` VARCHAR(32) DEFAULT NULL COMMENT 'create_source',
    `gmt_create` BIGINT(20) NOT NULL COMMENT '创建时间',
    `gmt_modified` BIGINT(20) NOT NULL COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_info_kptenantid` (`kp`, `tenant_id`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='tenant_info';

-- 用户表
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
    `username` VARCHAR(50) NOT NULL COMMENT 'username',
    `password` VARCHAR(500) NOT NULL COMMENT 'password',
    `enabled` TINYINT(1) NOT NULL COMMENT 'enabled',
    PRIMARY KEY (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='users';

-- 角色表
DROP TABLE IF EXISTS `roles`;
CREATE TABLE `roles` (
    `username` VARCHAR(50) NOT NULL COMMENT 'username',
    `role` VARCHAR(50) NOT NULL COMMENT 'role',
    UNIQUE KEY `uk_username_role` (`username`, `role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='roles';

-- 权限表
DROP TABLE IF EXISTS `permissions`;
CREATE TABLE `permissions` (
    `role` VARCHAR(50) NOT NULL COMMENT 'role',
    `resource` VARCHAR(255) NOT NULL COMMENT 'resource',
    `action` VARCHAR(8) NOT NULL COMMENT 'action',
    UNIQUE KEY `uk_role_permission` (`role`, `resource`, `action`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='permissions';

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
                                       file_name VARCHAR(500) COMMENT '原始文件名',
                                       file_type VARCHAR(20) COMMENT '文件类型（如：pdf、docx、md）',
                                       file_size BIGINT DEFAULT 0 COMMENT '文件大小（字节）',
                                       word_count INT DEFAULT 0 COMMENT '文档字数',
                                       chunk_count INT DEFAULT 0 COMMENT '导入的分片数量',
                                       status TINYINT DEFAULT 0 COMMENT '文档状态（0-待处理、1-已导入、2-导入失败）',
                                       error_msg TEXT COMMENT '错误信息',
                                       create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                       update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                       deleted TINYINT DEFAULT 0 COMMENT '是否删除（0-否、1-是）',
                                       INDEX idx_document_id (document_id),
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


-- 完成提示
SELECT '数据库初始化完成!' AS message;



