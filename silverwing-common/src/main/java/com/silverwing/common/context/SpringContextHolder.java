package com.silverwing.common.context;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * Spring 容器全局持有者
 * <p>
 * 提供任意位置访问 Bean 的能力，避免静态可变状态。
 * 适用于非 Spring 管理的类（如工具类、异常类）中获取 Bean。
 * </p>
 *
 * <pre>
 * MessageSource source = SpringContextHolder.getBean(MessageSource.class);
 * </pre>
 *
 * @author silverwing
 */
@Component
public class SpringContextHolder implements ApplicationContextAware {

    private static ApplicationContext context;

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        SpringContextHolder.context = applicationContext;
    }

    /**
     * 按 Class 获取 Bean
     *
     * @param clazz Bean 类型
     * @param <T>   泛型
     * @return Bean 实例
     * @throws IllegalStateException 容器未就绪时抛出
     */
    public static <T> T getBean(Class<T> clazz) {
        if (context == null) {
            throw new IllegalStateException("SpringContextHolder 尚未初始化，请确认 Spring 容器已启动");
        }
        return context.getBean(clazz);
    }

    /**
     * 容器是否已就绪
     */
    public static boolean isReady() {
        return context != null;
    }
}
