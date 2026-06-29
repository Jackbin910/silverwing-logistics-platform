package com.silverwing.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

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
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("银翼智驭医流综合管理平台API文档")
                        .version("1.0.0")
                        .description("医院智能物流综合管理平台接口文档")
                        .contact(new Contact()
                                .name("银翼科技")
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
