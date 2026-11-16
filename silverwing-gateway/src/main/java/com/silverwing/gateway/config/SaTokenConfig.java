package com.silverwing.gateway.config;

import cn.dev33.satoken.reactor.context.SaReactorSyncHolder;
import cn.dev33.satoken.reactor.filter.SaReactorFilter;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.ServerWebExchange;

/**
 * Sa-Token 配置（Gateway 层 - 响应式）
 * <p>
 * 认证拦截统一在 Gateway 层完成：
 * - 默认所有接口需要登录
 * - 白名单路径免登录（login、logout、doc、actuator 等）
 * - 未登录返回 401 JSON，前端拦截器检测后跳转登录页
 * </p>
 * <p>
 * 白名单必须通过 {@link SaReactorFilter#addExclude(String...)} 交给框架原生机制处理：
 * SaReactorFilter 内部固定执行 match(includeList).notMatch(excludeList).check(...)，
 * 在 setAuth 里再嵌套一层 SaRouter.notMatch 属于多余写法，1.45 版本下不生效。
 * </p>
 */
@Configuration
@Slf4j
public class SaTokenConfig {

    /**
     * 构建网关认证过滤器。
     *
     * @return SaReactorFilter 实例
     */
    @Bean
    public SaReactorFilter getSaReactorFilter() {
        return new SaReactorFilter()
                // 拦截所有路径
                .addInclude("/**")
                // 白名单交给框架原生 excludeList 处理，命中后直接跳过校验
                .addExclude(GatewayPublicPaths.allPublicPatterns())
                // 白名单之外的请求统一校验登录状态
                .setAuth(obj -> StpUtil.checkLogin())
                // 异常处理：Sa-Token 异常统一返回 401 JSON
                .setError(e -> {
                    log.warn("Gateway 认证拦截 path={}：{}", currentPath(), e.getMessage());
                    // SaResult.error 默认 code=500，前端按 401 识别登录过期并跳转登录页，这里必须显式置为 401
                    return SaResult.error("登录已过期，请重新登录").setCode(401);
                });
    }

    /**
     * 读取当前被拦截的请求路径，便于确认白名单是否命中。
     *
     * @return 请求路径，读取失败返回 unknown
     */
    private String currentPath() {
        try {
            ServerWebExchange exchange = SaReactorSyncHolder.getExchange();
            return exchange == null ? "unknown" : exchange.getRequest().getPath().value();
        } catch (Exception e) {
            return "unknown";
        }
    }

}
