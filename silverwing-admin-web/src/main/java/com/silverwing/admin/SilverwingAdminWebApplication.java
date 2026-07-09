package com.silverwing.admin;

import org.dromara.dynamictp.spring.annotation.EnableDynamicTp;
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
@EnableDynamicTp
@MapperScan("com.silverwing.biz.iam.infrastructure.dao")
@ComponentScan(basePackages = {"com.silverwing.admin", "com.silverwing.common", "com.silverwing.biz.iam"})
public class SilverwingAdminWebApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(SilverwingAdminWebApplication.class, args);
    }
    
}
