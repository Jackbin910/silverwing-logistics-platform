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
 * @author silverwing
 */
@AutoConfiguration
@ConditionalOnClass({RedisTemplate.class, RedisConnectionFactory.class})
public class RedisAutoConfiguration {

    /**
     * 显式声明并优先使用 Lettuce 连接工厂（带连接池）。
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
