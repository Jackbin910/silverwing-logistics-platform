package com.silverwing.common.config.serialize;

import cn.hutool.core.date.DatePattern;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.nio.charset.StandardCharsets;

/**
 * 基于 FastJSON2 的 Redis 序列化器
 * <p>
 * 用于 RedisTemplate 的 value 与 hashValue 序列化。
 * 相比 JDK 原生序列化，JSON 格式可读性更好、跨语言兼容、体积更小。
 * </p>
 * <p>
 * 序列化约定与 {@link com.silverwing.common.config.FastJson2WebMvcAutoConfiguration} 保持一致：
 * <ul>
 *   <li>日期格式：{@code yyyy-MM-dd HH:mm:ss}</li>
 *   <li>字符集：UTF-8</li>
 *   <li>Null 值：保留 null 字段，String→""，List→[]，Number→0，Boolean→false</li>
 * </ul>
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

    /**
     * 序列化（写）特性，统一 null 值处理与日期格式
     */
    private final JSONWriter.Feature[] writerFeatures = {
            // 保留 null 字段，不忽略
            JSONWriter.Feature.WriteMapNullValue,
            // null String 输出 ""
            JSONWriter.Feature.WriteNullStringAsEmpty,
            // null List/数组 输出 []
            JSONWriter.Feature.WriteNullListAsEmpty,
            // null Number 输出 0
            JSONWriter.Feature.WriteNullNumberAsZero,
            // null Boolean 输出 false
            JSONWriter.Feature.WriteNullBooleanAsFalse
    };

    /**
     * 反序列化（读）特性
     */
    private final JSONReader.Feature[] readerFeatures = {
            // 智能匹配字段名（下划线转驼峰等）
            JSONReader.Feature.SupportSmartMatch,
            // 忽略非 Serializable 类型的字段
            JSONReader.Feature.IgnoreNoneSerializable
    };

    public FastJson2RedisSerializer(Class<T> clazz) {
        super();
        this.clazz = clazz;
    }

    @Override
    public byte[] serialize(T t) throws SerializationException {
        String json = JSON.toJSONString(t, DatePattern.NORM_DATETIME_PATTERN, writerFeatures);
        return json.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public T deserialize(byte[] bytes) throws SerializationException {
        if (bytes.length == 0) {
            return null;
        }
        return JSON.parseObject(bytes, clazz, readerFeatures);
    }
}
