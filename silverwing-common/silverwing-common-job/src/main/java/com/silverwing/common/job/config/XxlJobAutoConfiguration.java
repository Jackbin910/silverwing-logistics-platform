package com.silverwing.common.job.config;

import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * XXL-Job 自动配置：根据 {@link XxlJobProperties} 创建执行器（XxlJobSpringExecutor）。
 * 引入本模块并配置 {@code xxl.job.*} 后即可直接使用 {@code @XxlJob} 注解声明任务。
 *
 * @author silverwing
 */
@AutoConfiguration
@ConditionalOnClass(XxlJobSpringExecutor.class)
@EnableConfigurationProperties(XxlJobProperties.class)
public class XxlJobAutoConfiguration {

    /**
     * 注册 XXL-Job Spring 执行器。
     *
     * @param properties XXL-Job 配置属性
     * @return XXL-Job 执行器
     */
    @Bean
    public XxlJobSpringExecutor xxlJobExecutor(XxlJobProperties properties) {
        XxlJobSpringExecutor executor = new XxlJobSpringExecutor();
        executor.setAdminAddresses(properties.getAdminAddresses());
        executor.setAppname(properties.getAppname());
        executor.setAddress(properties.getAddress());
        executor.setIp(properties.getIp());
        executor.setPort(properties.getPort());
        executor.setAccessToken(properties.getAccessToken());
        executor.setLogPath(properties.getLogPath());
        executor.setLogRetentionDays(properties.getLogRetentionDays());
        return executor;
    }
}
