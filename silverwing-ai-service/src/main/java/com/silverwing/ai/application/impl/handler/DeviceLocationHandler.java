package com.silverwing.ai.application.impl.handler;

import com.silverwing.ai.client.DeviceClient;
import com.silverwing.ai.application.dto.BizQueryResult;
import com.silverwing.ai.application.dto.EntityResult;
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
 * 设备位置查询处理器
 * 意图：QUERY_DEVICE_LOCATION
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceLocationHandler implements IntentHandler {

    private final DeviceClient deviceClient;

    @Override
    public IntentEnum getIntent() {
        return IntentEnum.QUERY_DEVICE_LOCATION;
    }

    @Override
    public BizQueryResult handle(List<EntityResult> entities) {
        String deviceId = IntentRouter.requireEntity(entities, EntityTypeEnum.DEVICE_ID, "设备编码");

        Result<Map<String, Object>> result = deviceClient.getLocation(deviceId);
        Map<String, Object> data = result.getData();

        log.info("设备位置查询完成, deviceId={}, location={}-{}",
                deviceId, data.get("warehouse"), data.get("area"));

        return BizQueryResult.builder()
                .title("设备位置")
                .data(data)
                .build();
    }
}
