-- ============================================================
-- 菜单/权限种子数据（sys_permission）
-- 解决前端登录后左侧菜单只有"首页"的问题：
-- 原 init.sql 未初始化菜单数据，getRouters 返回空树。
-- 执行后 admin 用户（走 isAdmin 全量加载）即可看到左侧菜单。
-- 注意：deleted 列由逻辑删除注解管理，默认未删除为 0。
--       create_by/update_by/create_time/update_time 由 MybatisPlus 自动填充，可不显式插入。
-- ============================================================

-- 顶级目录：系统管理
INSERT INTO sys_permission
    (permission_code, permission_name, resource_type, parent_id, sort, status, visible, icon, path, component, route_name, is_frame, is_cache, perms)
VALUES
    ('system', '系统管理', 'menu', 0, 1, 1, 0, 'system', 'system', 'Layout', 'System', 1, 1, NULL);

-- 系统管理下的二级菜单（父级 id 取上一条记录的 id，这里用变量兼容）
SET @system_id = (SELECT id FROM sys_permission WHERE permission_code = 'system');

INSERT INTO sys_permission
    (permission_code, permission_name, resource_type, parent_id, sort, status, visible, icon, path, component, route_name, is_frame, is_cache, perms)
VALUES
    ('system:user', '用户管理', 'menu', @system_id, 1, 1, 0, 'user', 'user', 'system/user/index', 'User', 1, 1, 'system:user:list'),
    ('system:role', '角色管理', 'menu', @system_id, 2, 1, 0, 'peoples', 'role', 'system/role/index', 'Role', 1, 1, 'system:role:list'),
    ('system:menu', '菜单管理', 'menu', @system_id, 3, 1, 0, 'tree-table', 'menu', 'system/menu/index', 'Menu', 1, 1, 'system:menu:list'),
    ('system:dept', '部门管理', 'menu', @system_id, 4, 1, 0, 'tree', 'dept', 'system/dept/index', 'Dept', 1, 1, 'system:dept:list'),
    ('system:post', '岗位管理', 'menu', @system_id, 5, 1, 0, 'post', 'post', 'system/post/index', 'Post', 1, 1, 'system:post:list'),
    ('system:dict', '字典管理', 'menu', @system_id, 6, 1, 0, 'dict', 'dict', 'system/dict/index', 'Dict', 1, 1, 'system:dict:list'),
    ('system:config', '参数设置', 'menu', @system_id, 7, 1, 0, 'edit', 'config', 'system/config/index', 'Config', 1, 1, 'system:config:list'),
    ('system:notice', '通知公告', 'menu', @system_id, 8, 1, 0, 'message', 'notice', 'system/notice/index', 'Notice', 1, 1, 'system:notice:list');

-- 顶级目录：系统监控（前端实际路径：在线用户在 monitor/，操作/登录日志在 system/）
INSERT INTO sys_permission
    (permission_code, permission_name, resource_type, parent_id, sort, status, visible, icon, path, component, route_name, is_frame, is_cache, perms)
VALUES
    ('monitor', '系统监控', 'menu', 0, 2, 1, 0, 'monitor', 'monitor', 'Layout', 'Monitor', 1, 1, NULL);

SET @monitor_id = (SELECT id FROM sys_permission WHERE permission_code = 'monitor');

INSERT INTO sys_permission
    (permission_code, permission_name, resource_type, parent_id, sort, status, visible, icon, path, component, route_name, is_frame, is_cache, perms)
VALUES
    ('monitor:online', '在线用户', 'menu', @monitor_id, 1, 1, 0, 'online', 'online', 'monitor/online/index', 'Online', 1, 1, 'monitor:online:list');

-- 操作日志 / 登录日志：前端实际位于 src/views/system/ 下，故挂在系统管理目录
INSERT INTO sys_permission
    (permission_code, permission_name, resource_type, parent_id, sort, status, visible, icon, path, component, route_name, is_frame, is_cache, perms)
VALUES
    ('system:operlog', '操作日志', 'menu', @system_id, 9, 1, 0, 'form', 'operlog', 'system/operlog/index', 'Operlog', 1, 1, 'system:operlog:list'),
    ('system:logininfor', '登录日志', 'menu', @system_id, 10, 1, 0, 'logininfor', 'logininfor', 'system/logininfor/index', 'Logininfor', 1, 1, 'system:logininfor:list');

-- admin 角色默认启用状态已在 init.sql 中（role_code=ADMIN），getRouters 对 admin 走全量加载，
-- 因此无需额外写入 sys_role_permission 关联即可展示全部菜单。
-- 若需给非 admin 用户授权，再按需插入 sys_role_permission 关联记录。
