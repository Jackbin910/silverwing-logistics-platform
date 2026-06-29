package com.silverwing.common.config;

import com.silverwing.common.config.serialize.FastJson2RedisSerializer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.StringUtils;

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
     * 显式声明并优先使用 Lettuce 连接工厂。
     * <p>
     * 解决 Redisson 等第三方注入的 RedisConnectionFactory 与 Spring Data Redis 版本
     * 不兼容导致 pExpire 递归栈溢出的问题。
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

        LettuceClientConfiguration.LettuceClientConfigurationBuilder builder =
                LettuceClientConfiguration.builder();
        if (redisProperties.getTimeout() != null) {
            builder.commandTimeout(redisProperties.getTimeout());
        }

        LettuceConnectionFactory connectionFactory =
                new LettuceConnectionFactory(standaloneConfiguration, builder.build());
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
