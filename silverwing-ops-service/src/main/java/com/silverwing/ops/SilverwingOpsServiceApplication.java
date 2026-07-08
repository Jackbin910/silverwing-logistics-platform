package com.silverwing.ops;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;

/**
 * 售后运维服务启动类
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.silverwing.common.infrastructure.mapper")
@ComponentScan(basePackages = {"com.silverwing.ops", "com.silverwing.common"})
public class SilverwingOpsServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(SilverwingOpsServiceApplication.class, args);
    }
    
}
