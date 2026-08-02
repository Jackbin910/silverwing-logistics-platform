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

    /**
     * Session 中存放登录用户名的键（供审计字段 createBy/updateBy、操作人等使用）
     */
    public static final String USERNAME = "username";

    /**
     * Session 中存放登录 IP 的键（在线用户监控使用）
     */
    public static final String LOGIN_IP = "loginIp";

    /**
     * Session 中存放登录浏览器的键（在线用户监控使用）
     */
    public static final String LOGIN_BROWSER = "loginBrowser";

    /**
     * Session 中存放登录操作系统的键（在线用户监控使用）
     */
    public static final String LOGIN_OS = "loginOs";

    /**
     * 验证码在 Redis 中的键前缀（后接 uuid），默认过期 2 分钟
     */
    public static final String CAPTCHA_PREFIX = "silverwing:captcha:";

    /**
     * 验证码默认过期时间（秒）
     */
    public static final long CAPTCHA_EXPIRE_SECONDS = 120L;

    private SaSessionConstants() {
    }

}
