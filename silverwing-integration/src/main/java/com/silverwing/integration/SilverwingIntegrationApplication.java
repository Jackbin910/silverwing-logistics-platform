package com.silverwing.integration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;

/**
 * 接入与集成服务启动类
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.silverwing.common.infrastructure.mapper")
@ComponentScan(basePackages = {"com.silverwing.integration", "com.silverwing.common"})
public class SilverwingIntegrationApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(SilverwingIntegrationApplication.class, args);
    }
    
}
