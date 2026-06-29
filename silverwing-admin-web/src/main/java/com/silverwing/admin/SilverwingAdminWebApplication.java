package com.silverwing.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * 管理后台Web服务启动类
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@ComponentScan(basePackages = {"com.silverwing.admin", "com.silverwing.common"})
public class SilverwingAdminWebApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(SilverwingAdminWebApplication.class, args);
    }
    
}
