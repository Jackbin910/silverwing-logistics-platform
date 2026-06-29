package com.silverwing.ai.client;

import com.silverwing.common.domain.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 订单服务 Feign 降级工厂
 */
@Slf4j
@Component
public class OrderClientFallback implements FallbackFactory<OrderClient> {

    @Override
    public OrderClient create(Throwable cause) {
        log.error("订单服务调用失败，启用降级策略", cause);
        return orderNo -> {
            log.warn("降级：使用模拟数据查询订单 {}", orderNo);
            return Result.success(Map.of(
                "orderNo", orderNo,
                "status", "配送中",
                "from", "中心药房",
                "to", "急诊科3楼护士站",
                "device", "AGV-005",
                "progress", "65%",
                "estimatedArrival", "约3分钟后到达"
            ));
        };
    }
}
