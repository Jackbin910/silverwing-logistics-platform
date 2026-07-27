package com.silverwing.ops.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 开门请求
 * 作为 ops-service 对外 Feign 契约的请求体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpenWarehouseRequest {

    /**
     * 库位编号，例如 12-A-01
     */
    private String location;

    private String requestId;
}
