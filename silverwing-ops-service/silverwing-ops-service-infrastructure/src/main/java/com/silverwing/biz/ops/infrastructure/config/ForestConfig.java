package com.silverwing.biz.ops.infrastructure.config;

import com.dtflys.forest.springboot.annotation.ForestScan;
import org.springframework.context.annotation.Configuration;

/**
 * Forest 配置
 * 扫描 H800 转换服务 Forest 接口所在包。
 * <p>
 * 转换服务地址由 {@code H800ConverterServiceApi} 上的
 * {@code @Address(source = H800AddressSource.class)} 指定，
 * 实际的 host/port 在 {@code H800AddressSource} 中从配置项
 * h800.converter.url 动态读取，因此此处无需手动注入 Forest 变量。
 * </p>
 */
@Configuration
@ForestScan("com.silverwing.biz.ops.infrastructure.adapter.h800.api")
public class ForestConfig {
}
