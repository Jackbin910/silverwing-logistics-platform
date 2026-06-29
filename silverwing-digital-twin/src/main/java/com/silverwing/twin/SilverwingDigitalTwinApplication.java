package com.silverwing.twin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * 数字孪生服务启动类
 */
@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = {"com.silverwing.twin", "com.silverwing.common"})
public class SilverwingDigitalTwinApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(SilverwingDigitalTwinApplication.class, args);
    }
    
}
