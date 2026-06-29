package com.silverwing.gateway.filter;

import cn.dev33.satoken.stp.StpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;

import java.net.InetSocketAddress;

/**
 * 日志全局过滤器
 * <p>
 * 记录所有请求的详细信息，包括：请求方法、路径、来源、耗时、响应状态等。
 * </p>
 */
@Slf4j
@Component
public class LogGlobalFilter implements GlobalFilter, Ordered {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String ANONYMOUS_USER = "anonymous";
    private static final String UNKNOWN_REMOTE_ADDR = "unknown";

    /**
     * 记录请求入口与出口日志。
     *
     * @param exchange 当前请求上下文
     * @param chain    Gateway 过滤器链
     * @return 过滤器执行结果
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String method = request.getMethod().name();
        String path = request.getPath().value();
        String remoteAddr = getRemoteAddr(request);
        String requestId = request.getId();
        String loginId = resolveLoginId(request);
        long startTime = System.currentTimeMillis();

        log.info("请求开始 requestId={} method={} path={} remoteAddr={} user={}",
                requestId, method, path, remoteAddr, loginId);

        return chain.filter(exchange)
                // 使用 doFinally 确保成功、异常、取消都只记录一次响应日志。
                .doFinally(signalType -> logResponse(
                        method, path, requestId, remoteAddr, loginId, startTime, exchange, signalType));
    }

    /**
     * 记录响应日志，避免在响应回调中访问 Sa-Token 请求上下文。
     *
     * @param method     请求方法
     * @param path       请求路径
     * @param requestId  请求 ID
     * @param remoteAddr 客户端地址
     * @param loginId    登录用户 ID
     * @param startTime  开始时间戳
     * @param exchange   当前请求上下文
     * @param signalType Reactor 结束信号
     */
    private void logResponse(String method, String path, String requestId, String remoteAddr, String loginId,
                             long startTime, ServerWebExchange exchange, SignalType signalType) {
        try {
            long duration = System.currentTimeMillis() - startTime;
            int statusCode = getStatusCode(exchange, signalType);

            log.info("请求结束 requestId={} method={} path={} status={} duration={}ms "
                            + "remoteAddr={} user={} signal={}",
                    requestId, method, path, statusCode, duration, remoteAddr, loginId, signalType);
        } catch (Exception e) {
            log.error("记录响应日志失败 requestId={} path={}", requestId, path, e);
        }
    }

    /**
     * 解析客户端地址。
     *
     * @param request 当前请求
     * @return 客户端 IP，解析失败时返回 unknown
     */
    private String getRemoteAddr(ServerHttpRequest request) {
        try {
            InetSocketAddress remoteAddress = request.getRemoteAddress();
            if (remoteAddress == null || remoteAddress.getAddress() == null) {
                return UNKNOWN_REMOTE_ADDR;
            }
            return remoteAddress.getAddress().getHostAddress();
        } catch (Exception e) {
            log.debug("解析客户端地址失败：{}", e.getMessage());
            return UNKNOWN_REMOTE_ADDR;
        }
    }

    /**
     * 从请求 token 中解析登录用户 ID，不依赖 Sa-Token 当前请求上下文。
     *
     * @param request 当前请求
     * @return 登录用户 ID，未登录或解析失败时返回 anonymous
     */
    private String resolveLoginId(ServerHttpRequest request) {
        try {
            String token = getTokenValue(request);
            if (token == null) {
                return ANONYMOUS_USER;
            }

            Object loginId = StpUtil.getLoginIdByToken(token);
            return loginId == null ? ANONYMOUS_USER : String.valueOf(loginId);
        } catch (Exception e) {
            log.debug("解析日志用户失败：{}", e.getMessage());
            return ANONYMOUS_USER;
        }
    }

    /**
     * 从请求头读取 token 值。
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
     * 获取响应状态码。
     *
     * @param exchange   当前请求上下文
     * @param signalType Reactor 结束信号
     * @return 响应状态码
     */
    private int getStatusCode(ServerWebExchange exchange, SignalType signalType) {
        try {
            return exchange.getResponse().getStatusCode().value();
        } catch (Exception e) {
            return SignalType.ON_ERROR.equals(signalType) ? 500 : -1;
        }
    }

    /**
     * 获取过滤器执行顺序。
     *
     * @return 过滤器顺序
     */
    @Override
    public int getOrder() {
        // 尽早记录入口日志，不阻塞认证过滤器执行。
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
