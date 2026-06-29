package com.silverwing.common.interceptor;

import cn.dev33.satoken.stp.StpUtil;
import com.silverwing.common.annotation.NeedLogin;
import com.silverwing.common.annotation.NeedPermission;
import com.silverwing.common.annotation.SkipAuth;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;

/**
 * 认证拦截器
 * <p>
 * 统一处理三种认证注解：
 * <ul>
 *   <li>{@link SkipAuth}：标记接口免登录，适用于登录、登出、公开查询等</li>
 *   <li>{@link NeedLogin}：显式标记接口需要登录（默认即需登录，可省略）</li>
 *   <li>{@link NeedPermission}：标记接口需要特定权限，配合 Sa-Token 的权限数据源校验</li>
 * </ul>
 * </p>
 * <p>
 * 认证策略优先级：
 * 1. 非控制器方法（静态资源等）直接放行
 * 2. 标记 @SkipAuth 的方法/类放行
 * 3. 标记 @NeedPermission 的方法/类，先校验登录再校验权限
 * 4. 其余方法（含 @NeedLogin 或无注解）默认校验登录
 * </p>
 *
 * @author silverwing
 */
@Slf4j
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 非控制器方法（如静态资源、错误页面），直接放行
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;

        // 1. 检查方法或类上是否有 @SkipAuth 注解，免登录直接放行
        if (isAnnotationPresent(handlerMethod, SkipAuth.class)) {
            log.debug("免登录接口：{} {}", request.getMethod(), request.getRequestURI());
            return true;
        }

        // 2. 检查 @NeedPermission 注解，先校验登录再校验权限
        NeedPermission needPermission = getAnnotation(handlerMethod, NeedPermission.class);
        if (needPermission != null) {
            // 权限校验前置：必须先登录
            StpUtil.checkLogin();
            // 校验权限标识，支持单个或多个（逗号分隔，需全部具备）
            String permission = needPermission.value();
            String[] permissions = permission.split(",");
            Arrays.stream(permissions)
                    .map(String::trim)
                    .filter(p -> !p.isEmpty())
                    .forEach(StpUtil::checkPermission);
            return true;
        }

        // 3. 默认需要登录（@NeedLogin 仅作显式标记，行为与默认一致）
        StpUtil.checkLogin();
        return true;
    }

    /**
     * 判断方法或类上是否存在指定注解
     */
    private boolean isAnnotationPresent(HandlerMethod handlerMethod,
                                        Class<? extends java.lang.annotation.Annotation> annotationType) {
        return handlerMethod.hasMethodAnnotation(annotationType)
                || handlerMethod.getBeanType().isAnnotationPresent(annotationType);
    }

    /**
     * 获取方法或类上的指定注解（方法级优先）
     */
    private <A extends java.lang.annotation.Annotation> A getAnnotation(HandlerMethod handlerMethod,
                                                                        Class<A> annotationType) {
        A methodAnnotation = handlerMethod.getMethodAnnotation(annotationType);
        if (methodAnnotation != null) {
            return methodAnnotation;
        }
        return handlerMethod.getBeanType().getAnnotation(annotationType);
    }
}
