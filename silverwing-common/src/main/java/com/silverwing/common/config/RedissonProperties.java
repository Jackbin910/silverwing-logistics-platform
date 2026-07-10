package com.silverwing.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Redisson 配置属性（用于分布式锁等场景）。
 * <p>
 * 连接地址复用 Spring 的 {@code spring.data.redis.*} 配置，
 * 本类仅用于调优连接池与锁相关的非连接参数。
 * 如需覆盖默认值，可在各服务 {@code application.yml} 中通过 {@code silverwing.redisson.*} 配置。
 *
 * @author silverwing
 */
@ConfigurationProperties(prefix = "silverwing.redisson")
@Data
public class RedissonProperties {

    /** 是否启用 SSL（使用 rediss:// 协议） */
    private boolean ssl = false;

    /** 最大连接数（连接池上限） */
    private int connectionPoolSize = 16;

    /** 最小空闲连接数 */
    private int connectionMinimumIdleSize = 4;

    /** 发布/订阅专用连接池大小 */
    private int subscriptionConnectionPoolSize = 4;

    /** 空闲连接超时时间（毫秒） */
    private int idleConnectionTimeout = 10000;

    /** 连接超时时间（毫秒） */
    private int connectTimeout = 10000;

    /** 命令执行超时时间（毫秒） */
    private int timeout = 3000;

    /** 命令重试次数 */
    private int retryAttempts = 3;

    /** 命令重试间隔（毫秒） */
    private int retryInterval = 1500;

    /** 心跳检测间隔（毫秒），0 表示关闭 */
    private int pingConnectionInterval = 30000;

    /** 看门狗锁续期超时时间（毫秒），用于分布式锁自动续期 */
    private long lockWatchdogTimeout = 30000L;

    /** 通用线程池大小 */
    private int threads = 16;

    /** Netty 线程池大小 */
    private int nettyThreads = 32;

}
