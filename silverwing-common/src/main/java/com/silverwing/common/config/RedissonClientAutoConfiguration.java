package com.silverwing.common.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.util.StringUtils;

/**
 * Redisson 自动配置（公共配置）。
 * <p>
 * 复用 Spring 的 {@code spring.data.redis} 连接信息构建 {@link RedissonClient}，
 * 并内置一套适合分布式锁场景的连接池与看门狗参数。
 * 后续业务可直接注入 {@code RedissonClient} 使用 {@code getLock()}/{@code getFairLock()} 等分布式锁。
 * <p>
 * 本配置类由各服务组件扫描加载（与 {@code RedisAutoConfiguration} 一致的加载方式）。
 * 由于 redisson-spring-boot-starter 自带自动配置声明了
 * {@code @ConditionalOnMissingBean(RedissonClient.class)}，本 Bean 会优先生效，避免重复创建。
 * <p>
 * 使用示例：
 * <pre>{@code
 *     @Autowired
 *     private RedissonClient redissonClient;
 *
 *     RLock lock = redissonClient.getLock("order:lock:" + orderId);
 *     try {
 *         // 未指定 leaseTime 时由看门狗自动续期
 *         lock.lock();
 *         // 业务处理...
 *     } finally {
 *         if (lock.isHeldByCurrentThread()) {
 *             lock.unlock();
 *         }
 *     }
 * }</pre>
 *
 * @author silverwing
 */
@AutoConfiguration
@ConditionalOnClass(RedissonClient.class)
@EnableConfigurationProperties(RedissonProperties.class)
public class RedissonClientAutoConfiguration {

    private final RedisProperties redisProperties;
    private final RedissonProperties redissonProperties;

    public RedissonClientAutoConfiguration(
            RedisProperties redisProperties,
            RedissonProperties redissonProperties) {
        this.redisProperties = redisProperties;
        this.redissonProperties = redissonProperties;
    }

    /**
     * 构建 RedissonClient 单例，供分布式锁等场景使用。
     * <p>
     * 连接相关参数（host/port/database/username/password）取自 Spring 的 Redis 配置，
     * 连接池与超时等调优参数取自 {@link RedissonProperties}。
     *
     * @return RedissonClient 实例
     */
    @Bean(destroyMethod = "shutdown")
    @Primary
    @ConditionalOnMissingBean(RedissonClient.class)
    public RedissonClient redissonClient() {
        Config config = new Config();
        RedissonProperties props = redissonProperties;

        String address = "redis://" + redisProperties.getHost() + ':' + redisProperties.getPort();

        // 当前平台使用单机版 Redis（与 RedisAutoConfiguration 中
        // RedisStandaloneConfiguration 保持一致）
        SingleServerConfig single = config.useSingleServer()
                .setAddress(address)
                .setDatabase(redisProperties.getDatabase())
                .setConnectionPoolSize(props.getConnectionPoolSize())
                .setConnectionMinimumIdleSize(props.getConnectionMinimumIdleSize())
                .setSubscriptionConnectionPoolSize(props.getSubscriptionConnectionPoolSize())
                .setIdleConnectionTimeout(props.getIdleConnectionTimeout())
                .setConnectTimeout(props.getConnectTimeout())
                .setTimeout(props.getTimeout())
                .setRetryAttempts(props.getRetryAttempts())
                .setRetryInterval(props.getRetryInterval())
                .setPingConnectionInterval(props.getPingConnectionInterval());

        if (StringUtils.hasText(redisProperties.getUsername())) {
            single.setUsername(redisProperties.getUsername());
        }
        if (StringUtils.hasText(redisProperties.getPassword())) {
            single.setPassword(redisProperties.getPassword());
        }

        // 看门狗锁续期：持有锁期间自动续期，避免业务未执行完锁提前过期
        config.setLockWatchdogTimeout(props.getLockWatchdogTimeout());
        config.setThreads(props.getThreads());
        config.setNettyThreads(props.getNettyThreads());

        return Redisson.create(config);
    }
}
