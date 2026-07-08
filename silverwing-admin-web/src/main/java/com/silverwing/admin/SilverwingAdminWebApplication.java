package com.silverwing.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.mybatis.spring.annotation.MapperScan;

/**
 * 管理后台Web服务启动类
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@MapperScan("com.silverwing.common.infrastructure.mapper")
@ComponentScan(basePackages = {"com.silverwing.admin", "com.silverwing.common"})
public class SilverwingAdminWebApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(SilverwingAdminWebApplication.class, args);
    }
    
}
