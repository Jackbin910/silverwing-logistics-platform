package com.silverwing.common.util;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis 工具类
 * <p>
 * 封装 RedisTemplate 与 RedissonClient 常用操作，简化业务层调用。
 * 通过 {@link com.silverwing.common.config.RedisUtilAutoConfiguration} 注册为 Bean，
 * 仅在 classpath 存在 RedisTemplate 与 RedissonClient 时加载，
 * 不使用 Redis 的服务不会因找不到依赖而启动失败。
 * </p>
 *
 * @author silverwing
 */
@Slf4j
public class RedisUtil {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedissonClient redissonClient;

    @Autowired
    public RedisUtil(RedisTemplate<String, Object> redisTemplate, RedissonClient redissonClient) {
        this.redisTemplate = redisTemplate;
        this.redissonClient = redissonClient;
    }

    // ==================== 基础操作 ====================

    /**
     * 设置缓存
     */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 设置缓存并设置过期时间（秒）
     */
    public void set(String key, Object value, long timeout) {
        redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.SECONDS);
    }

    /**
     * 设置缓存并设置过期时间（自定义单位）
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     * 获取缓存（泛型返回，调用方需自行强转）
     *
     * @param key 缓存键
     * @param <T> 期望的返回类型
     * @return 缓存值，不存在返回 null
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) redisTemplate.opsForValue().get(key);
    }

    /**
     * 获取缓存原始对象
     */
    public Object getObject(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 删除缓存
     */
    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    /**
     * 判断 key 是否存在
     */
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 设置过期时间（秒）
     */
    public Boolean expire(String key, long timeout) {
        return redisTemplate.expire(key, timeout, TimeUnit.SECONDS);
    }

    /**
     * 设置过期时间（自定义单位）
     */
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }

    /**
     * 获取剩余过期时间（秒）
     */
    public Long getExpire(String key) {
        return redisTemplate.getExpire(key);
    }

    /**
     * 自增
     */
    public Long increment(String key) {
        return redisTemplate.opsForValue().increment(key);
    }

    /**
     * 自增指定步长
     */
    public Long increment(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    // ==================== 分布式锁 ====================

    /**
     * 获取分布式锁对象（需调用方自行 try-finally unlock）
     */
    public RLock getLock(String lockKey) {
        return redissonClient.getLock(lockKey);
    }

    /**
     * 尝试获取分布式锁
     *
     * @param lockKey   锁键
     * @param waitTime  最大等待时间（秒）
     * @param leaseTime 持锁时间（秒）
     * @return 是否成功获取锁
     */
    public boolean tryLock(String lockKey, long waitTime, long leaseTime) {
        try {
            RLock lock = getLock(lockKey);
            return lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("获取分布式锁异常 lockKey={}：{}", lockKey, e.getMessage(), e);
            // 恢复中断标记，遵循中断响应最佳实践
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * 释放分布式锁（仅当锁由当前线程持有时）
     */
    public void unlock(String lockKey) {
        try {
            RLock lock = getLock(lockKey);
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        } catch (Exception e) {
            log.error("释放分布式锁异常 lockKey={}：{}", lockKey, e.getMessage(), e);
        }
    }
}
