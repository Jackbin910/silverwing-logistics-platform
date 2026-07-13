package com.silverwing.common.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

/**
 * 监控默认配置后置处理器
 * <p>
 * 以最低优先级注入 Actuator / Micrometer 默认配置，使所有微服务零改动即可暴露
 * {@code /actuator/health} 与 {@code /actuator/prometheus} 端点。
 * </p>
 * <p>
 * 由于使用 {@code addLast} 注入，任何服务自身的 {@code application.yml} 或
 * Nacos 远程配置均可覆盖这些默认值，符合「约定优于配置」原则。
 * </p>
 * <p>
 * 注意：若服务在自身配置中显式声明了 {@code management.endpoints.web.exposure.include}，
 * 将完全覆盖此处的默认值，需自行确保包含 {@code prometheus}。
 * </p>
 *
 * @author silverwing
 */
public class MonitorDefaultsEnvironmentPostProcessor implements EnvironmentPostProcessor {

    /** 默认配置 PropertySource 名称，便于调试时定位 */
    private static final String PROPERTY_SOURCE_NAME = "silverwing-monitor-defaults";

    /**
     * 注入监控默认配置
     *
     * @param environment   可配置环境
     * @param application   Spring 应用启动器
     */
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        // 若已有同名 PropertySource 则跳过，避免重复注入
        if (environment.getPropertySources().contains(PROPERTY_SOURCE_NAME)) {
            return;
        }

        Map<String, Object> defaults = new HashMap<>(8);
        // Actuator 端点基础路径
        defaults.put("management.endpoints.web.base-path", "/actuator");
        // 暴露健康检查、应用信息、Prometheus 指标端点
        defaults.put("management.endpoints.web.exposure.include", "health,info,prometheus,dynamictp");
        // 健康检查显示完整细节与组件信息，便于排障
        defaults.put("management.endpoint.health.show-details", "always");
        defaults.put("management.endpoint.health.show-components", "always");
        // 为所有 Micrometer 指标附加 application 公共标签，便于 Prometheus 按服务名聚合
        // 占位符由 Spring 在属性解析阶段替换为实际服务名
        defaults.put("management.metrics.tags.application", "${spring.application.name}");

        // addLast 确保优先级最低，可被 application.yml 与 Nacos 配置覆盖
        environment.getPropertySources()
                .addLast(new MapPropertySource(PROPERTY_SOURCE_NAME, defaults));
    }
}
