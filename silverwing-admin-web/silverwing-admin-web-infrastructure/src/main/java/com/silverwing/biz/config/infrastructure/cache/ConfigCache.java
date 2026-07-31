package com.silverwing.biz.config.infrastructure.cache;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CreateCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 参数配置缓存（按参数键名缓存键值对），用于减少热点参数的数据库访问。
 * <p>refreshCache 会清空全部已缓存的参数配置。</p>
 */
@Slf4j
@Component
public class ConfigCache {

    @CreateCache(name = "sys:config:")
    private Cache<String, String> configCache;

    private final Set<String> trackedKeys = ConcurrentHashMap.newKeySet();

    /**
     * 获取缓存中的参数键值，未命中返回 null。
     */
    public String get(String configKey) {
        return configCache.get(configKey);
    }

    /**
     * 写入缓存并记录已缓存的参数键名。
     */
    public void put(String configKey, String configValue) {
        configCache.put(configKey, configValue);
        trackedKeys.add(configKey);
    }

    /**
     * 移除指定参数键名的缓存。
     */
    public void remove(String configKey) {
        configCache.remove(configKey);
        trackedKeys.remove(configKey);
    }

    /**
     * 清空全部参数配置缓存。
     */
    public void clear() {
        try {
            if (!trackedKeys.isEmpty()) {
                configCache.removeAll(trackedKeys);
            }
        } catch (Exception e) {
            log.warn("清空参数配置缓存失败", e);
        }
        trackedKeys.clear();
    }
}
