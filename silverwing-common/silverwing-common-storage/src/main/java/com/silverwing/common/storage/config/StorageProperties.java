package com.silverwing.common.storage.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 对象存储配置属性
 * <p>
 * 通过 {@code silverwing.storage.*} 前缀绑定，兼容 RustFS / MinIO 等 S3 协议存储。
 * 默认关闭（enabled=false），避免无对象存储依赖时服务启动失败。
 * </p>
 *
 * @author silverwing
 */
@Data
@ConfigurationProperties(prefix = "silverwing.storage")
public class StorageProperties {

    /** 是否启用对象存储（默认关闭） */
    private boolean enabled = false;

    /** 存储服务端点（如 http://rustfs:9000） */
    private String endpoint = "http://rustfs:9000";

    /** 访问密钥 */
    private String accessKey = "rustfsadmin";

    /** 私密密钥 */
    private String secretKey = "rustfsadmin";

    /** 存储桶名称 */
    private String bucket = "silverwing";

    /** 区域（S3 协议要求，RustFS 可任意填写，默认 us-east-1） */
    private String region = "us-east-1";

    /** 是否使用路径风格访问（RustFS / MinIO 必须开启） */
    private boolean pathStyleAccess = true;

    /** 启动时是否自动创建存储桶 */
    private boolean autoCreateBucket = true;
}
