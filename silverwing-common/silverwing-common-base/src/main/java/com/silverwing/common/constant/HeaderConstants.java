package com.silverwing.common.constant;

/**
 * HTTP 请求头常量
 * <p>
 * 集中管理自定义请求头名称，避免在 Filter、Interceptor、Feign 拦截器中硬编码字符串。
 * 与日志链路追踪、用户上下文透传相关。
 * </p>
 *
 * @author silverwing
 */
public final class HeaderConstants {

    /**
     * 链路追踪 ID 请求头（网关生成，透传至下游服务）
     */
    public static final String X_TRACE_ID = "X-Trace-Id";

    /**
     * Span ID 请求头（链路追踪跨度 ID）
     */
    public static final String X_SPAN_ID = "X-Span-Id";

    /**
     * 用户 ID 请求头（网关鉴权后透传至下游服务，供业务读取）
     */
    public static final String X_USER_ID = "X-User-Id";

    /**
     * 用户名请求头（网关鉴权后透传至下游服务）
     */
    public static final String X_USERNAME = "X-Username";

    /**
     * 用户角色请求头（网关鉴权后透传至下游服务，多个角色逗号分隔）
     */
    public static final String X_USER_ROLES = "X-User-Roles";

    private HeaderConstants() {
    }
}
