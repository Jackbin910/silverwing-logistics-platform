package com.silverwing.common.i18n;

import com.silverwing.common.context.SpringContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

/**
 * 国际化消息工具类（无状态版）
 * <p>
 * 通过 SpringContextHolder 懒加载 MessageSource，避免静态可变状态。
 * 可在 Service、异常处理等非 Bean 场景下直接静态调用。
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
public final class MessageUtils {

    private MessageUtils() {
    }

    /**
     * 懒加载获取 MessageSource，容器未就绪时返回 null
     */
    private static MessageSource messageSource() {
        if (!SpringContextHolder.isReady()) {
            return null;
        }
        return SpringContextHolder.getBean(MessageSource.class);
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
        MessageSource source = messageSource();
        if (source == null) {
            log.warn("MessageSource 尚未就绪，返回原始 code: {}", code);
            return code;
        }
        try {
            Locale locale = LocaleContextHolder.getLocale();
            return source.getMessage(code, args, code, locale);
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
        MessageSource source = messageSource();
        if (source == null) {
            return code;
        }
        try {
            return source.getMessage(code, null, code, locale);
        } catch (Exception e) {
            log.warn("获取国际化文案失败 code={}, locale={}: {}", code, locale, e.getMessage());
            return code;
        }
    }
}
