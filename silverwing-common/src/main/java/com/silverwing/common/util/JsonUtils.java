package com.silverwing.common.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.TypeReference;

import java.util.List;

/**
 * JSON 工具类（基于 FastJSON2）
 * <p>
 * 统一项目内 JSON 序列化/反序列化入口，避免各处直接调用 FastJSON2 原生 API
 * 导致序列化特性不一致。所有方法均经过异常包装，调用方无需处理 checked exception。
 * </p>
 *
 * @author silverwing
 */
public final class JsonUtils {

    /**
     * 序列化特性：输出格式化、写入空集合、忽略空字段外的默认行为
     */
    private static final JSONWriter.Feature[] WRITE_FEATURES = {
            JSONWriter.Feature.WriteNullListAsEmpty,
            JSONWriter.Feature.WriteNullStringAsEmpty,
            JSONWriter.Feature.WriteMapNullValue
    };

    /**
     * 反序列化特性：忽略未知字段，提升兼容性
     */
    private static final JSONReader.Feature[] READ_FEATURES = {
            JSONReader.Feature.IgnoreNoneSerializable,
            JSONReader.Feature.SupportSmartMatch
    };

    private JsonUtils() {
    }

    /**
     * 对象序列化为 JSON 字符串
     *
     * @param obj 任意对象
     * @return JSON 字符串，obj 为 null 时返回 "null"
     */
    public static String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        return JSON.toJSONString(obj, WRITE_FEATURES);
    }

    /**
     * JSON 字符串反序列化为指定类型
     *
     * @param json  JSON 字符串
     * @param clazz 目标类型
     * @param <T>   泛型
     * @return 反序列化对象，json 为空时返回 null
     */
    public static <T> T parse(String json, Class<T> clazz) {
        if (json == null || json.isBlank()) {
            return null;
        }
        return JSON.parseObject(json, clazz, READ_FEATURES);
    }

    /**
     * JSON 字符串反序列化为泛型类型（支持 List、Map 等复杂泛型）
     *
     * @param json         JSON 字符串
     * @param typeReference 泛型类型引用，如 new TypeReference&lt;List&lt;User&gt;&gt;(){}
     * @param <T>          泛型
     * @return 反序列化对象
     */
    public static <T> T parse(String json, TypeReference<T> typeReference) {
        if (json == null || json.isBlank()) {
            return null;
        }
        return JSON.parseObject(json, typeReference, READ_FEATURES);
    }

    /**
     * JSON 字符串反序列化为 List
     *
     * @param json        JSON 字符串
     * @param elementType 列表元素类型
     * @param <T>         元素泛型
     * @return 列表，json 为空时返回空列表
     */
    public static <T> List<T> parseList(String json, Class<T> elementType) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        return JSON.parseArray(json, elementType);
    }
}
