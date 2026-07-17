package com.silverwing.ai.application.impl.handler;

import com.silverwing.ai.client.DeviceClient;
import com.silverwing.ai.domain.model.BizQueryResult;
import com.silverwing.ai.domain.model.EntityResult;
import com.silverwing.biz.ai.domain.enums.EntityTypeEnum;
import com.silverwing.biz.ai.domain.enums.IntentEnum;
import com.silverwing.ai.application.impl.IntentHandler;
import com.silverwing.ai.application.impl.IntentRouter;
import com.silverwing.common.domain.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 设备状态查询处理器
 * 意图：QUERY_DEVICE_STATUS
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceStatusHandler implements IntentHandler {

    private final DeviceClient deviceClient;

    @Override
    public IntentEnum getIntent() {
        return IntentEnum.QUERY_DEVICE_STATUS;
    }

    @Override
    public BizQueryResult handle(List<EntityResult> entities) {
        String deviceId = IntentRouter.requireEntity(entities, EntityTypeEnum.DEVICE_ID, "设备编码");

        Result<Map<String, Object>> result = deviceClient.getStatus(deviceId);
        Map<String, Object> data = result.getData();

        log.info("设备状态查询完成, deviceId={}, status={}", deviceId, data.get("status"));

        return BizQueryResult.builder()
                .title("设备状态")
                .data(data)
                .build();
    }
}
