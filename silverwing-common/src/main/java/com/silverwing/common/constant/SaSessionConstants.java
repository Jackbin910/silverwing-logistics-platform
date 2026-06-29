package com.silverwing.common.constant;

/**
 * Sa-Token 会话(Session)键常量
 * <p>
 * 登录时将用户角色、权限写入 Session（基于 Redis 共享），
 * 各微服务的 StpInterface 实现从此处定义的键中读取，实现跨服务权限校验。
 * </p>
 */
public class SaSessionConstants {

    /**
     * Session 中存放角色编码列表的键
     */
    public static final String ROLE_LIST = "roleList";

    /**
     * Session 中存放权限标识列表的键
     */
    public static final String PERMISSION_LIST = "permissionList";

    private SaSessionConstants() {
    }

}
