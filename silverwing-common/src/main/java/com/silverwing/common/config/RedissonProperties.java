package com.silverwing.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Redisson 配置属性（用于分布式锁等场景）。
 * <p>
 * 连接地址默认复用 Spring 的 {@code spring.data.redis} 配置，
 * 本类仅用于调优连接池、超时与锁相关参数。各参数默认值均对齐
 * Redisson 3.52 官方推荐值，并按微服务多实例场景做了适当收敛。
 * 如需覆盖默认值，可在各服务 {@code application.yml} 中通过 {@code silverwing.redisson.*} 配置。
 *
 * @author silverwing
 */
@ConfigurationProperties(prefix = "silverwing.redisson")
@Getter
@Setter
public class RedissonProperties {

    /** 是否启用 SSL（使用 rediss:// 协议） */
    private boolean ssl = false;

    /** 最大连接数（连接池上限），Redisson 默认 64，微服务场景收敛为 32 */
    private int connectionPoolSize = 32;

    /** 最小空闲连接数，Redisson 默认 24，收敛为 8 */
    private int connectionMinimumIdleSize = 8;

    /** 发布/订阅专用连接池大小，Redisson 默认 50，收敛为 16 */
    private int subscriptionConnectionPoolSize = 16;

    /** 发布/订阅最小空闲连接数，Redisson 默认 1 */
    private int subscriptionConnectionMinimumIdleSize = 4;

    /** 空闲连接超时时间（毫秒），Redisson 默认 10000 */
    private int idleConnectionTimeout = 10000;

    /** 连接超时时间（毫秒），Redisson 默认 10000 */
    private int connectTimeout = 10000;

    /** 命令执行超时时间（毫秒），Redisson 默认 3000 */
    private int timeout = 3000;

    /** 命令重试次数，Redisson 默认 4（退避策略使用 Redisson 默认 EqualJitterDelay） */
    private int retryAttempts = 4;

    /** 通用线程池大小，Redisson 默认 16 */
    private int threads = 16;

    /** Netty 线程池大小，Redisson 默认 32 */
    private int nettyThreads = 32;

    /** 是否开启 TCP KeepAlive，用于跨服务/容器网络下保持长连接，默认开启 */
    private boolean tcpKeepAlive = true;

}
