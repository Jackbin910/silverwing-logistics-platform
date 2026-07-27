package com.silverwing.ai.config;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.stp.StpUtil;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Feign 鉴权令牌透传拦截器（全局生效）
 *
 * <p>问题背景：本平台每个微服务都通过 common-web 的 AuthInterceptor 对所有请求做
 * Sa-Token 登录校验。外部请求经网关在 ai-service 完成鉴权后，ai-service 内部用 Feign
 * 调用 ops-service 等下游服务时若不携带令牌，下游会判定为未登录并返回 401
 * "登录已过期，请重新登录"。</p>
 *
 * <p>解决方式：在 Feign 发出请求前，把当前请求的有效令牌透传给下游。下游校验的是同一令牌，
 * 自然通过；同时保留操作人身份，开门等操作日志的审计信息不丢失。</p>
 */
@Slf4j
@Component
public class FeignAuthInterceptor implements RequestInterceptor {

    /**
     * 部分网关/前端使用 Authorization 头承载令牌，统一在此透传，确保与入站一致。
     */
    private static final String AUTHORIZATION = "Authorization";

    @Override
    public void apply(RequestTemplate template) {
        // 1. 优先复用 Sa-Token 当前上下文中的有效令牌，按 Sa-Token 配置的令牌名写入
        try {
            String token = StpUtil.getTokenValue();
            if (token != null && !token.isBlank()) {
                template.header(SaManager.getConfig().getTokenName(), token);
            }
        } catch (Exception e) {
            // 非 Web 上下文（如定时任务触发的 Feign 调用）取不到令牌，忽略，交由下游正常鉴权
            log.debug("Feign 令牌透传：当前上下文无 Sa-Token，跳过令牌注入");
        }

        // 2. 兜底：从入站请求的 Authorization 头原样透传
        ServletRequestAttributes attributes =
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String authorization = request.getHeader(AUTHORIZATION);
            if (authorization != null && !authorization.isBlank()) {
                template.header(AUTHORIZATION, authorization);
            }
        }
    }
}
