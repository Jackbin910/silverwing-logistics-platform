package com.silverwing.twin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;

/**
 * 数字孪生服务启动类
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.silverwing.common.infrastructure.mapper")
@ComponentScan(basePackages = {"com.silverwing.twin", "com.silverwing.common"})
public class SilverwingDigitalTwinApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(SilverwingDigitalTwinApplication.class, args);
    }
    
}
