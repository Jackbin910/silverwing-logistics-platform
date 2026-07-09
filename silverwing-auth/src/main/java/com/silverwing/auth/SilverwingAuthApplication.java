package com.silverwing.auth;

import org.dromara.dynamictp.spring.annotation.EnableDynamicTp;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * 认证授权服务启动类
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableDynamicTp
@MapperScan("com.silverwing.biz.iam.infrastructure.dao")
@ComponentScan(basePackages = {"com.silverwing.auth", "com.silverwing.common", "com.silverwing.biz.iam"})
public class SilverwingAuthApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(SilverwingAuthApplication.class, args);
    }
    
}
