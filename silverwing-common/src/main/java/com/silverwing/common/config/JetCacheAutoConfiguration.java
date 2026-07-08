package com.silverwing.common.config;

import com.alicp.jetcache.anno.config.EnableCreateCacheAnnotation;
import com.alicp.jetcache.anno.config.EnableMethodCache;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;

/**
 * JetCache 自动配置
 * <p>
 * 启用方法级缓存注解（@Cached / @CacheInvalidate / @CacheClear），
 * 所有微服务通过依赖 common 即可使用 JetCache 注解。
 * </p>
 * <p>
 * 需要在 Nacos 或 application.yml 中配置 jetcache 区域信息：
 * <pre>
 * jetcache:
 *   statIntervalMinutes: 15
 *   areaInCacheName: false
 *   remote:
 *     default:
 *       type: redis
 *       keyConvertor: fastjson
 *       valueEncoder: java
 *       valueDecoder: java
 *       expireAfterWriteInMillis: 300000   # 5 分钟
 * </pre>
 * </p>
 *
 * @author silverwing
 */
@AutoConfiguration
@ConditionalOnClass(EnableMethodCache.class)
@EnableMethodCache(basePackages = "com.silverwing")
@EnableCreateCacheAnnotation
public class JetCacheAutoConfiguration {
}
