package com.silverwing.ai.client;

import com.silverwing.common.domain.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * 工单服务 Feign 客户端
 * 调用 silverwing-ops-service 创建工单
 */
@FeignClient(
    name = "silverwing-ops-service",
    contextId = "workOrderClient",
    path = "/api/work-order",
    fallbackFactory = WorkOrderClientFallback.class
)
public interface WorkOrderClient {

    /**
     * 根据工单号查询工单详情
     *
     * @param workOrderNo 工单号
     * @return 工单信息
     */
    @GetMapping("/getByNo/{workOrderNo}")
    Result<Map<String, Object>> getByNo(@PathVariable("workOrderNo") String workOrderNo);

    /**
     * 创建工单
     *
     * @param params 工单参数
     * @return 创建结果
     */
    @PostMapping("/create")
    Result<Map<String, Object>> create(@RequestBody Map<String, Object> params);
}
