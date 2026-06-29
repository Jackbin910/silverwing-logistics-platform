package com.silverwing.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * AI分析服务启动类
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@ComponentScan(basePackages = {"com.silverwing.ai", "com.silverwing.common"})
public class SilverwingAiServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(SilverwingAiServiceApplication.class, args);
    }
    
}
