package com.silverwing.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * 核心业务服务启动类
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@ComponentScan(basePackages = {"com.silverwing.core", "com.silverwing.common"})
public class SilverwingCoreApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(SilverwingCoreApplication.class, args);
    }
    
}
