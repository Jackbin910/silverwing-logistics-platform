package com.silverwing.ai.client;

import com.silverwing.common.domain.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 工单服务 Feign 降级工厂
 */
@Slf4j
@Component
public class WorkOrderClientFallback implements FallbackFactory<WorkOrderClient> {

    @Override
    public WorkOrderClient create(Throwable cause) {
        log.error("工单服务调用失败，启用降级策略", cause);
        return new WorkOrderClient() {

            @Override
            public Result<Map<String, Object>> getByNo(String workOrderNo) {
                log.warn("降级：查询工单失败 workOrderNo={}", workOrderNo);
                return Result.fail("工单服务暂时不可用");
            }

            @Override
            public Result<Map<String, Object>> create(Map<String, Object> params) {
                String workOrderNo = "WO" + System.currentTimeMillis();
                log.warn("降级：使用模拟数据创建工单 {}", workOrderNo);
                return Result.success(Map.of(
                    "workOrderNo", workOrderNo,
                    "status", "已提交",
                    "estimatedResponse", "30分钟内"
                ));
            }
        };
    }
}
