package com.silverwing.gateway.filter;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 用户信息传递过滤器
 * <p>
 * 在请求转发到下游服务时，将用户ID等信息添加到请求头中
 * 便于下游服务获取当前用户上下文
 * </p>
 */
@Slf4j
@Component
public class UserContextFilter implements GlobalFilter, Ordered {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String DEFAULT_USER_TYPE = "DEFAULT";
    private static final String USER_TYPE_SESSION_KEY = "userType";
    private static final List<String> PUBLIC_PATHS = List.of(
            "/auth/login",
            "/auth/logout",
            "/doc.html",
            "/webjars/**",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/favicon.ico",
            "/actuator/**",
            "/error",
            "/static/**",
            "/public/**"
    );

    /**
     * 为已登录请求添加用户上下文请求头。
     *
     * @param exchange 当前请求上下文
     * @param chain    Gateway 过滤器链
     * @return 过滤器执行结果
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        try {
            String token = getTokenValue(request);
            if (token == null) {
                return chain.filter(exchange);
            }

            Object loginId = StpUtil.getLoginIdByToken(token);
            if (loginId == null) {
                return chain.filter(exchange);
            }

            String userId = String.valueOf(loginId);
            String userType = getUserType(loginId);
            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-Id", userId)
                    .header("X-User-Type", userType)
                    .build();

            log.debug("添加用户上下文：userId={}", userId);
            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        } catch (Exception e) {
            log.warn("添加用户上下文失败 path={}：{}", path, e.getMessage(), e);
            return chain.filter(exchange);
        }
    }

    /**
     * 从请求头中读取 token 值。
     *
     * @param request 当前请求
     * @return token 值，未携带时返回 null
     */
    private String getTokenValue(ServerHttpRequest request) {
        String token = request.getHeaders().getFirst(StpUtil.getTokenName());
        if (token == null || token.isBlank()) {
            return null;
        }

        token = token.trim();
        if (token.startsWith(BEARER_PREFIX)) {
            token = token.substring(BEARER_PREFIX.length()).trim();
        }
        return token.isEmpty() ? null : token;
    }

    /**
     * 读取用户类型，读取失败时返回默认值。
     *
     * @param loginId 登录 ID
     * @return 用户类型
     */
    private String getUserType(Object loginId) {
        try {
            SaSession session = StpUtil.getSessionByLoginId(loginId, false);
            if (session == null) {
                return DEFAULT_USER_TYPE;
            }

            String userType = session.getString(USER_TYPE_SESSION_KEY);
            return userType == null ? DEFAULT_USER_TYPE : userType;
        } catch (Exception e) {
            log.debug("读取用户类型失败 loginId={}：{}", loginId, e.getMessage());
            return DEFAULT_USER_TYPE;
        }
    }

    /**
     * 判断当前路径是否为免登录路径。
     *
     * @param path 请求路径
     * @return true 表示免登录路径
     */
    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(pattern -> {
            if (pattern.endsWith("/**")) {
                return path.startsWith(pattern.substring(0, pattern.length() - 3));
            }
            return path.equals(pattern);
        });
    }

    /**
     * 获取过滤器执行顺序。
     *
     * @return 过滤器顺序
     */
    @Override
    public int getOrder() {
        // 在认证之后、路由转发之前执行
        return 100;
    }
}
