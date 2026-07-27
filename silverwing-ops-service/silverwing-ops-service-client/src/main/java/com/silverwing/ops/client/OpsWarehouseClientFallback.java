package com.silverwing.ops.client;

import com.silverwing.common.domain.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * 运维服务开门 Feign 降级工厂
 * 服务不可用时返回失败信息，绝不伪造成功
 */
@Slf4j
@Component
public class OpsWarehouseClientFallback implements FallbackFactory<OpsWarehouseClient> {

    @Override
    public OpsWarehouseClient create(Throwable cause) {
        log.error("运维服务开门调用失败，启用降级策略", cause);
        return request -> {
            log.warn("降级：ops-service 不可用，未发送开门指令 location={}", request.getLocation());
            return Result.fail("运维服务暂不可用，开门指令未发送");
        };
    }
}
