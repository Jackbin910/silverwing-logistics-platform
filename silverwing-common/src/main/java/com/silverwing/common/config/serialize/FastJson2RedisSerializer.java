package com.silverwing.common.config.serialize;

import com.alibaba.fastjson2.JSON;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.nio.charset.StandardCharsets;

/**
 * 基于 FastJSON2 的 Redis 序列化器
 * <p>
 * 用于 RedisTemplate 的 value 与 hashValue 序列化。
 * 相比 JDK 原生序列化，JSON 格式可读性更好、跨语言兼容、体积更小。
 * </p>
 *
 * @param <T> 序列化目标类型
 * @author silverwing
 */
public class FastJson2RedisSerializer<T> implements RedisSerializer<T> {

    /**
     * 目标类型，反序列化时使用
     */
    private final Class<T> clazz;

    public FastJson2RedisSerializer(Class<T> clazz) {
        super();
        this.clazz = clazz;
    }

    @Override
    public byte[] serialize(T t) throws SerializationException {
        if (t == null) {
            return new byte[0];
        }
        return JSON.toJSONString(t).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public T deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        return JSON.parseObject(bytes, clazz);
    }
}
