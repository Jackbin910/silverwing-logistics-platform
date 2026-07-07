package com.silverwing.common.config;

import com.silverwing.common.config.serialize.FastJson2RedisSerializer;
import io.lettuce.core.api.StatefulConnection;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.StringUtils;

import java.time.Duration;

/**
 * Redis 自动配置类（公共配置）
 * <p>
 * 用于配置 RedisTemplate 序列化策略，所有微服务共享此配置。
 * 仅在 classpath 存在 Redis 相关类时加载，不使用 Redis 的服务不会触发。
 * </p>
 * <p>
 * 配置说明：
 * <ul>
 *   <li>强制使用 LettuceConnectionFactory 作为主连接工厂，避免第三方 RedisConnection
 *       实现与 Spring Data Redis 版本不兼容导致 pExpire 递归栈溢出</li>
 *   <li>启用 Lettuce 连接池，保持最小空闲连接，防止长时间无请求后冷启动超时</li>
 *   <li>Key 使用 StringRedisSerializer：保持 key 为可读字符串格式</li>
 *   <li>Value 使用 FastJson2RedisSerializer：基于 FastJSON2 的 JSON 序列化，性能更优</li>
 *   <li>Hash 结构同样采用上述策略</li>
 * </ul>
 * </p>
 *
 * @author silverwing
 */
@AutoConfiguration
@ConditionalOnClass({RedisTemplate.class, RedisConnectionFactory.class})
public class RedisAutoConfiguration {

    /**
     * 显式声明并优先使用 Lettuce 连接工厂（带连接池）。
     * <p>
     * 解决 Redisson 等第三方注入的 RedisConnectionFactory 与 Spring Data Redis 版本
     * 不兼容导致 pExpire 递归栈溢出的问题。
     * </p>
     * <p>
     * 连接池配置：min-idle=2 保持最少 2 个空闲连接，避免长时间无请求后
     * Redis 连接被断开导致首次请求超时。
     * </p>
     *
     * @param redisProperties Spring Redis 配置属性
     * @return Redis 连接工厂
     */
    @Bean
    @Primary
    public RedisConnectionFactory redisConnectionFactory(RedisProperties redisProperties) {
        RedisStandaloneConfiguration standaloneConfiguration = new RedisStandaloneConfiguration();
        standaloneConfiguration.setHostName(redisProperties.getHost());
        standaloneConfiguration.setPort(redisProperties.getPort());
        standaloneConfiguration.setDatabase(redisProperties.getDatabase());

        if (StringUtils.hasText(redisProperties.getUsername())) {
            standaloneConfiguration.setUsername(redisProperties.getUsername());
        }
        if (StringUtils.hasText(redisProperties.getPassword())) {
            standaloneConfiguration.setPassword(RedisPassword.of(redisProperties.getPassword()));
        }

        GenericObjectPoolConfig<StatefulConnection<?, ?>> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMinIdle(2);
        poolConfig.setMaxIdle(8);
        poolConfig.setMaxTotal(16);
        poolConfig.setTestWhileIdle(true);
        poolConfig.setTimeBetweenEvictionRuns(Duration.ofSeconds(30));
        poolConfig.setMinEvictableIdleDuration(Duration.ofMinutes(10));

        LettucePoolingClientConfiguration clientConfig = LettucePoolingClientConfiguration.builder()
                .poolConfig(poolConfig)
                .commandTimeout(redisProperties.getTimeout() != null
                        ? redisProperties.getTimeout() : Duration.ofSeconds(5))
                .build();

        LettuceConnectionFactory connectionFactory =
                new LettuceConnectionFactory(standaloneConfiguration, clientConfig);
        connectionFactory.setValidateConnection(true);
        return connectionFactory;
    }

    /**
     * 注册 Redis 消息监听容器。
     *
     * @param connectionFactory Redis 连接工厂
     * @return RedisMessageListenerContainer
     */
    @Bean
    public RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        return container;
    }

    /**
     * 注册 StringRedisTemplate。
     *
     * @param connectionFactory Redis 连接工厂
     * @return StringRedisTemplate
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }

    /**
     * 配置 RedisTemplate，使用 FastJSON2 序列化策略。
     *
     * @param redisConnectionFactory Redis 连接工厂
     * @return RedisTemplate
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        // 使用 StringRedisSerializer 序列化 key
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // 使用 FastJson2RedisSerializer 序列化 value（基于 FastJSON2，性能更优）
        FastJson2RedisSerializer<Object> fastJsonSerializer = new FastJson2RedisSerializer<>(Object.class);
        template.setValueSerializer(fastJsonSerializer);
        template.setHashValueSerializer(fastJsonSerializer);

        template.afterPropertiesSet();
        return template;
    }
}
