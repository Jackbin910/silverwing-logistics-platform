package com.silverwing.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;

/**
 * AI分析服务启动类
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@MapperScan("com.silverwing.biz.ai.infrastructure.mapper")
@ComponentScan(basePackages = {"com.silverwing.ai", "com.silverwing.common", "com.silverwing.biz.ai"})
public class SilverwingAiServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(SilverwingAiServiceApplication.class, args);
    }
    
}
