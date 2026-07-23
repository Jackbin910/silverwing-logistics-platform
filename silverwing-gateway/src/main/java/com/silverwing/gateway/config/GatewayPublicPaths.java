package com.silverwing.gateway.config;

import org.springframework.util.AntPathMatcher;

import java.util.List;

/**
 * 网关公开路径（白名单）统一定义。
 * <p>
 * Gateway 层存在两处白名单判断，语义一致（公开路径既免登录校验也无需注入用户上下文）：
 * 1. {@link SaTokenConfig} —— 决定哪些路径免登录校验（SaRouter.notMatch）；
 * 2. {@link com.silverwing.gateway.filter.UserContextFilter} —— 决定哪些路径跳过用户上下文注入。
 * 统一在此维护单一数据源，避免两处各自维护导致的不一致。
 * </p>
 */
public final class GatewayPublicPaths {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    /**
     * 免登录 / 免用户上下文注入的公开路径。
     * <p>
     * 同时包含「逐个服务的精确列举」与「跨服务通配」两类写法：精确列举保证 SaRouter 行为稳定，
     * </p>
     */
    public static final List<String> PUBLIC_PATHS = List.of(
            // 认证相关（auth 前缀，网关默认路由）
            "/auth/login",
            "/auth/logout",
            "/auth/public-key",
            // 认证相关（api 前缀，前端实际调用路径 /api/login 等）
            "/api/login",
            "/api/logout",
            "/api/public-key",
            // API 文档（精确列举，覆盖各服务）
            "/doc.html",
            "/webjars/**",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/auth/v3/api-docs/**",
            "/core/v3/api-docs/**",
            "/twin/v3/api-docs/**",
            "/ai/v3/api-docs/**",
            "/ops/v3/api-docs/**",
            "/integration/v3/api-docs/**",
            "/admin/v3/api-docs/**",
            "/auth/swagger-resources/**",
            "/core/swagger-resources/**",
            "/twin/swagger-resources/**",
            "/ai/swagger-resources/**",
            "/ops/swagger-resources/**",
            "/integration/swagger-resources/**",
            "/admin/swagger-resources/**",
            "/favicon.ico",
            "/actuator/**",
            "/error",
            "/static/**",
            "/public/**"
    );

    private GatewayPublicPaths() {
    }

    /**
     * 判断当前路径是否为公开路径（免登录）。
     *
     * @param path 请求路径
     * @return true 表示公开路径
     */
    public static boolean matches(String path) {
        return PUBLIC_PATHS.stream().anyMatch(pattern -> PATH_MATCHER.match(pattern, path));
    }
}
