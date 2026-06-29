-- ========================================
-- 银翼智驭 - 权限校验扩展脚本
-- 在 silverwing_logistics 库中执行
-- 可重复执行（使用 IF NOT EXISTS / INSERT IGNORE）
-- ========================================

USE silverwing_logistics;

-- ========================================
-- 权限表
-- ========================================
CREATE TABLE IF NOT EXISTS `sys_permission` (
    `id`            BIGINT NOT NULL AUTO_INCREMENT COMMENT '权限ID',
    `permission_code` VARCHAR(100) NOT NULL COMMENT '权限标识（如 system:user:list）',
    `permission_name` VARCHAR(100) NOT NULL COMMENT '权限名称（如 查询用户）',
    `resource_type` VARCHAR(20) NOT NULL DEFAULT 'api' COMMENT '资源类型: menu-菜单, button-按钮, api-接口',
    `parent_id`     BIGINT DEFAULT 0 COMMENT '父级ID，0为顶级',
    `sort`          INT DEFAULT 0 COMMENT '排序',
    `status`        TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    `deleted`       TINYINT DEFAULT 0 COMMENT '删除标记',
    `create_time`   DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_permission_code` (`permission_code`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- ========================================
-- 角色权限关联表
-- ========================================
CREATE TABLE IF NOT EXISTS `sys_role_permission` (
    `id`            BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `role_id`       BIGINT NOT NULL COMMENT '角色ID',
    `permission_id` BIGINT NOT NULL COMMENT '权限ID',
    `create_time`   DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`),
    KEY `idx_role_id` (`role_id`),
    KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- ========================================
-- 初始化权限数据
-- ========================================
INSERT IGNORE INTO `sys_permission` (`permission_code`, `permission_name`, `resource_type`, `parent_id`, `sort`) VALUES
-- 用户管理
('system:user:list',     '查询用户',     'api', 0, 1),
('system:user:add',      '新增用户',     'api', 0, 2),
('system:user:edit',     '编辑用户',     'api', 0, 3),
('system:user:delete',   '删除用户',     'api', 0, 4),
-- 角色管理
('system:role:list',     '查询角色',     'api', 0, 10),
('system:role:assign',   '分配角色',     'api', 0, 11),
-- 权限管理
('system:permission:manage', '权限管理', 'api', 0, 20),
-- 驾驶舱
('dashboard:overview',   '全院概览',     'api', 0, 30),
('dashboard:heatmap',    '物流热力图',   'api', 0, 31),
('dashboard:devicetree', '设备树',       'api', 0, 32),
-- 订单管理
('logistics:order:list',   '查询订单',   'api', 0, 40),
('logistics:order:create', '创建订单',   'api', 0, 41),
('logistics:order:cancel', '取消订单',   'api', 0, 42),
('logistics:order:track',  '订单追踪',   'api', 0, 43),
-- 设备管理
('logistics:device:list',   '查询设备',  'api', 0, 50),
('logistics:device:add',    '新增设备',  'api', 0, 51),
('logistics:device:edit',   '编辑设备',  'api', 0, 52),
('logistics:device:delete', '删除设备',  'api', 0, 53),
-- 工单管理
('ops:workorder:list',   '查询工单',     'api', 0, 60),
('ops:workorder:handle', '处理工单',     'api', 0, 61);

-- ========================================
-- 初始化角色权限关联
-- 角色: 1-ADMIN, 2-USER, 3-OPERATOR（来自 init.sql）
-- ========================================

-- ADMIN 拥有全部权限
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 1, id FROM `sys_permission` WHERE `deleted` = 0;

-- USER 拥有基础查看与下单权限
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 2, id FROM `sys_permission`
WHERE `permission_code` IN (
    'dashboard:overview',
    'dashboard:devicetree',
    'logistics:order:list',
    'logistics:order:create',
    'logistics:order:track',
    'logistics:device:list'
);

-- OPERATOR 拥有运营管理权限
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 3, id FROM `sys_permission`
WHERE `permission_code` IN (
    'dashboard:overview',
    'dashboard:heatmap',
    'dashboard:devicetree',
    'logistics:order:list',
    'logistics:order:create',
    'logistics:order:cancel',
    'logistics:order:track',
    'logistics:device:list',
    'ops:workorder:list',
    'ops:workorder:handle'
);

SELECT '权限表初始化完成!' AS message;
