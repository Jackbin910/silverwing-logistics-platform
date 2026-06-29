package com.silverwing.common.annotation;

import java.lang.annotation.*;

/**
 * 免登录注解
 * 标记此注解的接口不需要登录即可访问
 * 适用于登录、登出、公开查询等无需认证的场景
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SkipAuth {

}