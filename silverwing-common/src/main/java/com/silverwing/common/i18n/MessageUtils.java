package com.silverwing.common.i18n;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Locale;

/**
 * 国际化消息工具类
 * <p>
 * 提供静态方法，便于在 Service、异常处理等非 Bean 场景下获取多语言文案。
 * 通过 @PostConstruct 持有静态引用，解决 static 方法无法直接注入的问题。
 * </p>
 * <p>
 * 使用示例：
 * <pre>
 * MessageUtils.get("auth.login.success");
 * MessageUtils.get("auth.login.username.or.password.error");
 * </pre>
 * </p>
 *
 * @author silverwing
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessageUtils {

    private final MessageSource messageSource;

    private static MessageSource staticMessageSource;

    /**
     * 初始化静态引用，使工具方法可在任意位置调用
     */
    @PostConstruct
    public void init() {
        staticMessageSource = this.messageSource;
        log.info("MessageUtils 初始化完成，已绑定 MessageSource");
    }

    /**
     * 根据当前线程 Locale 获取消息文案
     *
     * @param code 消息 code，如 auth.login.success
     * @return 当前语言对应文案，找不到时返回 code 本身
     */
    public static String get(String code) {
        return get(code, (Object[]) null);
    }

    /**
     * 获取带占位符的消息文案
     * <p>properties 中定义: auth.welcome=欢迎, {0}</p>
     *
     * @param code 消息 code
     * @param args 占位符参数
     * @return 格式化后的文案
     */
    public static String get(String code, Object... args) {
        if (staticMessageSource == null) {
            log.warn("MessageSource 尚未初始化，返回原始 code: {}", code);
            return code;
        }
        try {
            Locale locale = LocaleContextHolder.getLocale();
            return staticMessageSource.getMessage(code, args, code, locale);
        } catch (Exception e) {
            log.warn("获取国际化文案失败 code={}: {}", code, e.getMessage());
            return code;
        }
    }

    /**
     * 指定 Locale 获取消息
     *
     * @param code   消息 code
     * @param locale 目标语言
     * @return 文案，找不到时返回 code 本身
     */
    public static String get(String code, Locale locale) {
        if (staticMessageSource == null) {
            return code;
        }
        try {
            return staticMessageSource.getMessage(code, null, code, locale);
        } catch (Exception e) {
            log.warn("获取国际化文案失败 code={}, locale={}: {}", code, locale, e.getMessage());
            return code;
        }
    }
}
