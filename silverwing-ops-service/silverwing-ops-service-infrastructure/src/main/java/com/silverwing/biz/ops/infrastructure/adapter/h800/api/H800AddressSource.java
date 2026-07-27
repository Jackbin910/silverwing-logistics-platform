package com.silverwing.biz.ops.infrastructure.adapter.h800.api;

import com.dtflys.forest.callback.AddressSource;
import com.dtflys.forest.http.ForestAddress;
import com.dtflys.forest.http.ForestRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * H800 转换服务动态地址源
 * <p>
 * @Address 的 host/port 分开写时，port 属性必须是字面数字，无法用 ${} 模板；
 * 且本版本 @Address 没有 value() 属性，不能写单个 URL 字符串。
 * 因此通过实现 Forest 的 {@link AddressSource}，在运行时返回完整地址（host + port），
 * 从而让地址真正取自配置项 h800.converter.url（形如 192.168.31.81:8081）。
 * </p>
 */
@Component
public class H800AddressSource implements AddressSource {

    /** 转换服务地址（h800.converter.url，形如 host:port） */
    @Value("${h800.converter.url:192.168.31.81:8081}")
    private String url;

    /**
     * 返回 H800 转换服务的地址
     *
     * @param request 当前 Forest 请求（未使用）
     * @return 由配置项解析出的 host + port 地址
     */
    @Override
    public ForestAddress getAddress(ForestRequest request) {
        // 兼容配置中是否带 http:// 前缀
        String raw = url;
        int schemeIdx = raw.indexOf("://");
        if (schemeIdx >= 0) {
            raw = raw.substring(schemeIdx + 3);
        }
        String[] parts = raw.split(":");
        String host = parts[0];
        int port = parts.length > 1 ? Integer.parseInt(parts[1].trim()) : 80;
        return new ForestAddress("http", host, port);
    }
}
