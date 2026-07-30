package com.silverwing.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.i18n.LocaleContextHolder;

/**
 * Knife4j / OpenAPI 自动配置
 * <p>
 * 仅在 classpath 存在 OpenAPI 相关类时加载，不使用 Knife4j 的服务不会触发。
 * </p>
 *
 * @author silverwing
 */
@AutoConfiguration
@ConditionalOnClass({OpenAPI.class, GroupedOpenApi.class})
public class Knife4jAutoConfiguration {

    /**
     * 配置 OpenAPI 文档基本信息
     * <p>
     * 标题/描述/联系方式走国际化：默认英文品牌名，中文环境由 zh_CN 文案回退为中文。
     * </p>
     */
    @Bean
    public OpenAPI customOpenAPI(MessageSource messageSource) {
        // 默认兜底使用英文品牌名，确保英文环境下展示 silverwing
        String title = messageSource.getMessage(
                "app.title", null, "SilverWing Logistics Platform API", LocaleContextHolder.getLocale());
        String description = messageSource.getMessage(
                "app.description", null,
                "Hospital intelligent logistics platform API documentation.", LocaleContextHolder.getLocale());
        String contactName = messageSource.getMessage(
                "app.contact.name", null, "SilverWing", LocaleContextHolder.getLocale());
        return new OpenAPI()
                .info(new Info()
                        .title(title)
                        .version("1.0.0")
                        .description(description)
                        .contact(new Contact()
                                .name(contactName)
                                .email("support@silverwing.com")));
    }

    /**
     * 配置 API 分组
     */
    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public")
                .pathsToMatch("/**")
                .build();
    }
}
