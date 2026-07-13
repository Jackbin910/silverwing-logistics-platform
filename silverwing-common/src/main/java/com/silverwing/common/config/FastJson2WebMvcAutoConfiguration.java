package com.silverwing.common.config;

import cn.hutool.core.date.DatePattern;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.support.config.FastJsonConfig;
import com.alibaba.fastjson2.support.spring6.http.converter.FastJsonHttpMessageConverter;
import com.silverwing.common.constant.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * FastJSON2 HTTP 消息转换器自动配置
 * <p>
 * 替换 Spring Boot 默认的 Jackson 转换器，统一全平台的 JSON 序列化/反序列化约定：
 * <ul>
 *   <li>日期格式：{@code yyyy-MM-dd HH:mm:ss}</li>
 *   <li>字符集：UTF-8</li>
 *   <li>Null 值处理：保留 null 字段，String 输出 ""，List/数组输出 []，
 *       Number 输出 0，Boolean 输出 false</li>
 * </ul>
 * </p>
 *
 * @author silverwing
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({FastJsonHttpMessageConverter.class, WebMvcConfigurer.class})
public class FastJson2WebMvcAutoConfiguration implements WebMvcConfigurer {

    private static final Logger log = LoggerFactory.getLogger(FastJson2WebMvcAutoConfiguration.class);

    /**
     * 扩展消息转换器列表，将 FastJSON2 转换器插入到首位，使其优先于 Jackson。
     * <p>
     * 使用 {@code extendMessageConverters} 而非 {@code configureMessageConverters}，
     * 以保留 Spring Boot 注册的默认转换器（如 StringHttpMessageConverter 等）。
     * </p>
     *
     * @param converters 已注册的 HTTP 消息转换器列表
     */
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        try {
            FastJsonHttpMessageConverter fastJsonConverter = buildFastJsonConverter();
            // 插入到首位，确保优先于 Jackson 处理 application/json
            converters.add(0, fastJsonConverter);
        } catch (Exception e) {
            log.error("注册 FastJSON2 HTTP 消息转换器失败，回退到默认 Jackson 转换器", e);
        }
    }

    /**
     * 构建 FastJSON2 HTTP 消息转换器，配置日期格式、字符集与 Null 值策略。
     *
     * @return 配置完成的 FastJsonHttpMessageConverter
     */
    private FastJsonHttpMessageConverter buildFastJsonConverter() {
        FastJsonHttpMessageConverter converter = new FastJsonHttpMessageConverter();
        converter.setFastJsonConfig(buildFastJsonConfig());

        List<MediaType> mediaTypes = new ArrayList<>();
        mediaTypes.add(MediaType.APPLICATION_JSON);
        mediaTypes.add(new MediaType("application", "*+json"));
        converter.setSupportedMediaTypes(mediaTypes);

        return converter;
    }

    /**
     * 构建 FastJSON2 配置，统一日期格式、字符集与 Null 值处理策略。
     *
     * @return 配置完成的 FastJsonConfig
     */
    private FastJsonConfig buildFastJsonConfig() {
        FastJsonConfig config = new FastJsonConfig();

        // 字符集统一使用 UTF-8
        config.setCharset(StandardCharsets.UTF_8);

        // 日期格式统一为 yyyy-MM-dd HH:mm:ss
        config.setDateFormat(DatePattern.NORM_DATETIME_PATTERN);

        // ---- 序列化（写）特性 ----
        config.setWriterFeatures(
                // 保留 null 字段，不忽略
                JSONWriter.Feature.WriteMapNullValue,
                // null String 输出 ""
                JSONWriter.Feature.WriteNullStringAsEmpty,
                // null List/数组 输出 []
                JSONWriter.Feature.WriteNullListAsEmpty,
                // null Number 输出 0
                JSONWriter.Feature.WriteNullNumberAsZero,
                // null Boolean 输出 false
                JSONWriter.Feature.WriteNullBooleanAsFalse,
                // 格式化输出（可读性优先，生产环境如需压缩可移除此项）
                JSONWriter.Feature.PrettyFormat
        );

        // ---- 反序列化（读）特性 ----
        config.setReaderFeatures(
                // 智能匹配字段名（下划线转驼峰等）
                JSONReader.Feature.SupportSmartMatch,
                // 忽略非 Serializable 类型的字段
                JSONReader.Feature.IgnoreNoneSerializable
        );

        return config;
    }
}
