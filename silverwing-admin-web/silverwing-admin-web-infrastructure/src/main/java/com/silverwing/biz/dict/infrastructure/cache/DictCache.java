package com.silverwing.biz.dict.infrastructure.cache;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CreateCache;
import com.silverwing.biz.dict.domain.model.aggregate.SysDictDataAggregate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 字典数据缓存（按字典类型缓存），用于减少热点字典类型的数据库访问。
 * <p>refreshCache 会清空全部已缓存的字典类型数据。</p>
 */
@Slf4j
@Component
public class DictCache {

    @CreateCache(name = "dict:data:byType:")
    private Cache<String, List<SysDictDataAggregate>> dataCache;

    private final Set<String> trackedTypes = ConcurrentHashMap.newKeySet();

    /**
     * 获取缓存中的字典数据，未命中返回 null。
     */
    public List<SysDictDataAggregate> get(String dictType) {
        return dataCache.get(dictType);
    }

    /**
     * 写入缓存并记录已缓存的字典类型。
     */
    public void put(String dictType, List<SysDictDataAggregate> list) {
        dataCache.put(dictType, list);
        trackedTypes.add(dictType);
    }

    /**
     * 清空全部字典缓存。
     */
    public void clear() {
        try {
            if (!trackedTypes.isEmpty()) {
                dataCache.removeAll(trackedTypes);
            }
        } catch (Exception e) {
            log.warn("清空字典缓存失败", e);
        }
        trackedTypes.clear();
    }
}
