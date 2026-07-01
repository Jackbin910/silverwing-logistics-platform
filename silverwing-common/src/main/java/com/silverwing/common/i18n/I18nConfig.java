package com.silverwing.common.i18n;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.nio.charset.StandardCharsets;

/**
 * 国际化消息源配置
 * <p>
 * 加载顺序：公共文案（common）+ 微服务专属文案（通过 silverwing.i18n.extra-basenames 扩展）。
 * 不同 basename 避免同名资源被覆盖，确保分层文案都能正确加载。
 * </p>
 *
 * @author silverwing
 */
@Configuration
@EnableConfigurationProperties(I18nProperties.class)
public class I18nConfig {

    /**
     * 配置消息源，支持多 basename 分层加载
     *
     * @param properties 国际化配置属性
     * @return MessageSource
     */
    @Bean
    public MessageSource messageSource(I18nProperties properties) {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        // 公共文案固定加载（common jar 自带）
        source.addBasenames("i18n/common");
        // 微服务专属文案（通过 yml 配置扩展）
        if (properties.getExtraBasenames() != null && !properties.getExtraBasenames().isEmpty()) {
            source.addBasenames(properties.getExtraBasenames().toArray(new String[0]));
        }
        // UTF-8 编码
        source.setDefaultEncoding(StandardCharsets.UTF_8.name());
        // 找不到对应语言时不回退到系统默认 Locale，直接回退到默认文件
        source.setFallbackToSystemLocale(false);
        // 找不到 code 时返回原 code 而非抛异常
        source.setUseCodeAsDefaultMessage(true);
        // 缓存秒数
        source.setCacheSeconds(properties.getCacheSeconds());
        return source;
    }
}
