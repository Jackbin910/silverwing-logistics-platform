package com.silverwing.common.i18n;

import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.concurrent.Callable;

/**
 * Locale 上下文传播工具
 * <p>
 * {@link LocaleContextHolder} 基于 ThreadLocal 存储当前请求语言，异步线程
 * （CompletableFuture / DynamicTP / MQ 消费 / 定时任务等）默认无法继承。
 * 本工具在提交异步任务的线程（通常为请求线程）捕获当前 {@link LocaleContext}，
 * 并在执行线程内临时绑定，执行结束后还原，避免污染线程池中的复用线程。
 * </p>
 * <p>
 * 使用示例：
 * <pre>
 * CompletableFuture.runAsync(LocaleContextUtils.wrap(() -&gt; {
 *     // 此处调用 MessageUtils.get(...) 可正确取到请求语言
 * }), executor);
 * </pre>
 * </p>
 *
 * @author silverwing
 */
public final class LocaleContextUtils {

    private LocaleContextUtils() {
    }

    /**
     * 包装 Runnable，使其在执行时继承调用线程的 Locale 上下文
     *
     * @param task 原始任务
     * @return 携带 Locale 上下文的任务
     */
    public static Runnable wrap(Runnable task) {
        // 在提交线程（请求线程）捕获当前语言上下文
        final LocaleContext captured = LocaleContextHolder.getLocaleContext();
        return () -> {
            // 保存执行线程原有上下文，执行后还原，避免污染线程池复用线程
            LocaleContext previous = LocaleContextHolder.getLocaleContext();
            LocaleContextHolder.setLocaleContext(captured);
            try {
                task.run();
            } finally {
                LocaleContextHolder.setLocaleContext(previous);
            }
        };
    }

    /**
     * 包装 Callable，使其在执行时继承调用线程的 Locale 上下文
     *
     * @param task 原始任务
     * @param <T>  返回值类型
     * @return 携带 Locale 上下文的任务
     */
    public static <T> Callable<T> wrap(Callable<T> task) {
        // 在提交线程（请求线程）捕获当前语言上下文
        final LocaleContext captured = LocaleContextHolder.getLocaleContext();
        return () -> {
            // 保存执行线程原有上下文，执行后还原，避免污染线程池复用线程
            LocaleContext previous = LocaleContextHolder.getLocaleContext();
            LocaleContextHolder.setLocaleContext(captured);
            try {
                return task.call();
            } finally {
                LocaleContextHolder.setLocaleContext(previous);
            }
        };
    }
}
