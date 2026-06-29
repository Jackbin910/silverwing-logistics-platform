package com.silverwing.ai.client;

import com.silverwing.common.domain.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * 订单服务 Feign 客户端
 * 调用 silverwing-core-service 查询订单信息
 */
@FeignClient(
    name = "silverwing-core-service",
    contextId = "orderClient",
    path = "/api/order",
    fallbackFactory = OrderClientFallback.class
)
public interface OrderClient {

    /**
     * 根据订单号查询订单详情
     *
     * @param orderNo 订单号
     * @return 订单信息
     */
    @GetMapping("/getByNo/{orderNo}")
    Result<Map<String, Object>> getByOrderNo(@PathVariable("orderNo") String orderNo);
}
