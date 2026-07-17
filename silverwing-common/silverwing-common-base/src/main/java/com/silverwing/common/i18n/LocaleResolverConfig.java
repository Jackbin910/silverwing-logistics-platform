package com.silverwing.common.i18n;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.List;
import java.util.Locale;

/**
 * Locale 解析器配置
 * <p>基于 Accept-Language 请求头解析用户语言，适用于微服务无状态架构</p>
 * <p>前端通过 axios 拦截器统一设置 Accept-Language 头即可切换语言</p>
 *
 * @author silverwing
 */
@Configuration
public class LocaleResolverConfig {

    /**
     * 基于 Accept-Language 请求头解析 Locale
     * <p>支持的语言白名单，防止非预期 Locale 导致 fallback 行为不确定</p>
     *
     * @return LocaleResolver
     */
    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        resolver.setSupportedLocales(
                List.of(Locale.SIMPLIFIED_CHINESE, Locale.US)
        );
        // 默认中文
        resolver.setDefaultLocale(Locale.SIMPLIFIED_CHINESE);
        return resolver;
    }
}
