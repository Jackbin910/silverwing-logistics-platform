package com.silverwing.common.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpInterface;
import com.silverwing.common.interceptor.AuthInterceptor;
import com.silverwing.common.stp.StpInterfaceImpl;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 自动配置（各微服务通用 - Servlet 模式）
 * <p>
 * 通过 Spring Boot 3 自动配置机制加载，确保所有依赖 silverwing-common 的服务
 * 都能正确注册认证拦截器与权限数据源。
 * </p>
 * <p>
 * 认证策略：默认所有接口需要登录，{@link com.silverwing.common.annotation.SkipAuth @SkipAuth}
 * 标注的接口免登录，{@link com.silverwing.common.annotation.NeedPermission @NeedPermission}
 * 标注的接口需校验权限。
 * </p>
 * <p>
 * 注意：此配置仅用于各微服务（auth、core-service 等），不适用于 Gateway。
 * Gateway 使用 WebFlux，不依赖 common，使用独立的 Sa-Token Reactor 配置。
 * </p>
 *
 * @author silverwing
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({StpInterface.class, WebMvcConfigurer.class})
public class SaTokenAutoConfiguration implements WebMvcConfigurer {

    /**
     * 注册拦截器
     * <ul>
     *   <li>AuthInterceptor：负责登录校验（默认需登录，@SkipAuth 免登录，@NeedPermission 校验权限）</li>
     *   <li>SaInterceptor：负责权限/角色注解校验（@SaCheckRole、@SaCheckPermission 等）</li>
     * </ul>
     *
     * @param registry 拦截器注册器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 1. 认证拦截器：默认需要登录，@SkipAuth 免登录，@NeedPermission 校验权限
        registry.addInterceptor(new AuthInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns(
                        // 基础设施路径（非控制器方法，无法加注解，需要排除）
                        "/doc.html",
                        "/webjars/**",
                        "/swagger-resources/**",
                        "/v3/api-docs/**",
                        "/favicon.ico",
                        "/actuator/**",
                        "/error"
                )
                .order(1);

        // 2. Sa-Token拦截器：仅处理权限/角色注解，不做路由级认证
        //    空的 handle 函数表示不在路由层面做任何认证，仅扫描注解
        registry.addInterceptor(new SaInterceptor(handle -> {}))
                .addPathPatterns("/**")
                .order(2);
    }

    /**
     * 注册 Sa-Token 权限/角色数据源
     * <p>
     * 从 Session（Redis 共享）读取权限与角色，支持跨服务校验。
     * </p>
     *
     * @return StpInterface 实现
     */
    @Bean
    public StpInterface stpInterface() {
        return new StpInterfaceImpl();
    }
}
