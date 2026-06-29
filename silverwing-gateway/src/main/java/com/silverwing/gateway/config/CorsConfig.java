package com.silverwing.gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * 跨域配置
 * <p>
 * 处理前端跨域请求，支持CORS（跨域资源共享）
 * 注意：生产环境应该限制 allowedOriginPatterns 为具体域名
 * </p>
 */
@Slf4j
@Configuration
public class CorsConfig {

    /**
     * 配置CORS过滤器
     * <p>
     * 配置说明：
     * - allowedOriginPatterns: 允许的来源域名（开发环境使用 *，生产环境需指定具体域名）
     * - allowedMethods: 允许的HTTP方法
     * - allowedHeaders: 允许的请求头
     * - exposedHeaders: 暴露给前端的响应头
     * - allowCredentials: 是否允许携带Cookie
     * - maxAge: 预检请求缓存时间（秒）
     * </p>
     */
    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // 开发环境允许所有域名，生产环境应限制为具体域名
        String corsAllowedOrigins = System.getenv("CORS_ALLOWED_ORIGINS");
        if (corsAllowedOrigins != null && !corsAllowedOrigins.isEmpty()) {
            // 从环境变量读取允许的域名列表，逗号分隔
            for (String origin : corsAllowedOrigins.split(",")) {
                config.addAllowedOrigin(origin.trim());
            }
        } else {
            // 默认允许所有域名（仅用于开发环境）
            config.addAllowedOriginPattern("*");
        }

        // 允许携带Cookie和凭证
        config.setAllowCredentials(true);

        // 允许所有请求方法
        config.addAllowedMethod("*");

        // 允许所有请求头
        config.addAllowedHeader("*");

        // 暴露给前端的响应头
        config.addExposedHeader("Authorization");
        config.addExposedHeader("Content-Type");
        config.addExposedHeader("X-Request-Id");

        // 预检请求缓存时间（2小时）
        config.setMaxAge(7200L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        log.info("CORS配置已加载，允许来源：{}", corsAllowedOrigins != null ? corsAllowedOrigins : "*");

        return new CorsWebFilter(source);
    }
}
