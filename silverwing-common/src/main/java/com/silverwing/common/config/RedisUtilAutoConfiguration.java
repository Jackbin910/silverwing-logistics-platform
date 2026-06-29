package com.silverwing.common.config;

import com.silverwing.common.util.RedisUtil;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * RedisUtil 自动配置
 * <p>
 * 仅在 classpath 同时存在 RedisTemplate 与 RedissonClient 时注册 RedisUtil Bean，
 * 避免不需要 Redis 的服务因依赖缺失而启动失败。
 * </p>
 *
 * @author silverwing
 */
@AutoConfiguration
@ConditionalOnClass({RedisTemplate.class, RedissonClient.class})
public class RedisUtilAutoConfiguration {

    /**
     * 注册 RedisUtil
     *
     * @param redisTemplate  Redis 操作模板
     * @param redissonClient Redisson 客户端
     * @return RedisUtil 实例
     */
    @Bean
    @ConditionalOnMissingBean
    public RedisUtil redisUtil(RedisTemplate<String, Object> redisTemplate,
                               RedissonClient redissonClient) {
        return new RedisUtil(redisTemplate, redissonClient);
    }
}
