package com.silverwing.gateway.config;

import cn.dev33.satoken.reactor.filter.SaReactorFilter;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.silverwing.gateway.config.GatewayPublicPaths;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Sa-Token 配置（Gateway 层 - 响应式）
 * <p>
 * 认证拦截统一在 Gateway 层完成：
 * - 默认所有接口需要登录
 * - 白名单路径免登录（login、logout、doc、actuator 等）
 * - 未登录返回 401 JSON，前端拦截器检测后跳转登录页
 * </p>
 */
@Configuration
@Slf4j
public class SaTokenConfig {

    @Bean
    public SaReactorFilter getSaReactorFilter() {
        return new SaReactorFilter()
                // 拦截所有路径
                .addInclude("/**")
                // 认证函数：排除白名单后，其余请求均需登录
                .setAuth(obj -> SaRouter
                        .match("/**")
                        // 白名单：以下路径无需登录即可访问
                        .notMatch(GatewayPublicPaths.PUBLIC_PATHS.toArray(new String[0]))
                        // 不在白名单内的，校验登录状态
                        .check(r -> StpUtil.checkLogin()))
                // 异常处理：Sa-Token 异常统一返回 401 JSON
                .setError(e -> {
                    log.warn("Gateway 认证拦截：{}", e.getMessage());
                    return SaResult.error("登录已过期，请重新登录");
                });
    }

}