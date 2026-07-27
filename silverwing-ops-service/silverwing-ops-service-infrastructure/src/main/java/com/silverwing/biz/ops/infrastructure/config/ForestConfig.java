package com.silverwing.biz.ops.infrastructure.config;

import com.dtflys.forest.springboot.annotation.ForestScan;
import org.springframework.context.annotation.Configuration;

/**
 * Forest 配置
 * 扫描 H800 转换服务 Forest 接口所在包
 */
@Configuration
@ForestScan("com.silverwing.biz.ops.infrastructure.adapter.h800.api")
public class ForestConfig {
}
