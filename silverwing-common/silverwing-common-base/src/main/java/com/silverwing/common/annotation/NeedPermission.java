package com.silverwing.common.annotation;

import java.lang.annotation.*;

/**
 * 需要权限注解
 * 用于标记需要特定权限才能访问的接口
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NeedPermission {
    
    /**
     * 权限标识
     */
    String value();
    
}
