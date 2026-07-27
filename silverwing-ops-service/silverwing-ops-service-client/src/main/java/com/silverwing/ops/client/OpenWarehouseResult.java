package com.silverwing.ops.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 开门结果
 * 作为 ops-service 对外 Feign 契约的响应体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpenWarehouseResult {

    /**
     * 是否成功触发开门指令
     */
    private Boolean success;

    /**
     * 请求唯一标识，便于日志追踪
     */
    private String requestId;

    /**
     * 提示信息
     */
    private String message;
}
