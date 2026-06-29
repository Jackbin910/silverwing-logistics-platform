package com.silverwing.common.annotation;

import java.lang.annotation.*;

/**
 * 需要登录注解
 * 用于标记需要登录才能访问的接口
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NeedLogin {
    
}
