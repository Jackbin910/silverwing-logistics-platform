package com.silverwing.ops.client;

import com.silverwing.common.domain.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 运维服务仓储开门 Feign 客户端
 * 供 ai-service 等内部服务调用 H800 开门能力
 */
@FeignClient(
    name = "silverwing-ops-service",
    contextId = "opsWarehouseClient",
    path = "/h800",
    fallbackFactory = OpsWarehouseClientFallback.class
)
public interface OpsWarehouseClient {

    /**
     * 打开指定库位的 H800 接驳仓门
     *
     * @param request 开门请求（含库位编号）
     * @return 统一返回结果
     */
    @PostMapping("/open")
    Result<OpenWarehouseResult> openWarehouse(@RequestBody OpenWarehouseRequest request);
}
