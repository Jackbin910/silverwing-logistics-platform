package com.silverwing.common.config;

import com.silverwing.common.filter.RequestTraceFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

/**
 * 链路追踪过滤器自动配置
 * <p>
 * 注册 {@link RequestTraceFilter}，为每个请求生成 traceId 写入 MDC 和响应头，
 * 配合 logback-spring.xml 实现统一日志格式。
 * </p>
 * <p>
 * 仅在 Servlet Web 环境加载，Gateway（WebFlux）不触发。
 * </p>
 *
 * @author silverwing
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class TraceFilterAutoConfiguration {

    /**
     * 注册请求链路 ID 过滤器
     *
     * @return FilterRegistrationBean
     */
    @Bean
    public FilterRegistrationBean<RequestTraceFilter> requestTraceFilterRegistration() {
        FilterRegistrationBean<RequestTraceFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RequestTraceFilter());
        registration.addUrlPatterns("/*");
        registration.setName("requestTraceFilter");
        // 最高优先级，确保 traceId 在所有逻辑之前生成
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }
}
