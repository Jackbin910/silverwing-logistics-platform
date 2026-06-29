package com.silverwing.common.config;

import com.silverwing.common.interceptor.FeignTraceInterceptor;
import feign.RequestInterceptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * Feign 链路追踪自动配置
 * <p>
 * 仅在 classpath 存在 Feign {@link RequestInterceptor} 时加载，
 * 注册 {@link FeignTraceInterceptor} 实现跨服务调用的 traceId 透传。
 * </p>
 * <p>
 * 不使用 OpenFeign 的服务（如 auth、digital-twin）不会触发本配置。
 * </p>
 *
 * @author silverwing
 */
@AutoConfiguration
@ConditionalOnClass(RequestInterceptor.class)
public class FeignTraceAutoConfiguration {

    /**
     * 注册 Feign 链路追踪拦截器
     *
     * @return FeignTraceInterceptor 实例
     */
    @Bean
    @ConditionalOnMissingBean(FeignTraceInterceptor.class)
    public FeignTraceInterceptor feignTraceInterceptor() {
        return new FeignTraceInterceptor();
    }
}
