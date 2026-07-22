package com.silverwing.common.storage.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

/**
 * 对象存储自动配置
 * <p>
 * 当 {@code silverwing.storage.enabled=true} 时生效，注册 {@link S3Client} 与
 * {@link com.silverwing.common.storage.core.FileStorageService} 两个 Bean。
 * 关闭时两个 Bean 均不创建，业务侧通过 {@code ObjectProvider} 优雅降级。
 * </p>
 *
 * @author silverwing
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "silverwing.storage.enabled", havingValue = "true")
@EnableConfigurationProperties(StorageProperties.class)
public class StorageAutoConfiguration {

    /**
     * 构建 S3 客户端（强制路径风格 + 自定义端点，适配 RustFS / MinIO）
     *
     * @param properties 存储配置
     * @return S3Client 实例
     */
    @Bean
    public S3Client s3Client(StorageProperties properties) {
        AwsBasicCredentials credentials =
                AwsBasicCredentials.create(properties.getAccessKey(), properties.getSecretKey());
        String endpoint = properties.getEndpoint();
        log.info("初始化对象存储客户端: endpoint={}, bucket={}", endpoint, properties.getBucket());
        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(properties.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .forcePathStyle(properties.isPathStyleAccess())
                .build();
    }

    /**
     * 构建文件存储服务
     *
     * @param s3Client   S3 客户端
     * @param properties 存储配置
     * @return FileStorageService 实例
     */
    @Bean
    public com.silverwing.common.storage.core.FileStorageService fileStorageService(
            S3Client s3Client, StorageProperties properties) {
        return new com.silverwing.common.storage.core.FileStorageService(s3Client, properties);
    }
}
