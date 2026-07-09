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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 设备指标查询处理器
 * 意图：QUERY_DEVICE_METRIC
 *
 * <p>
 * 查询设备的遥测指标数据（温度、电量、速度等）
 * 当前部分使用模拟数据，待 TimescaleDB 集成后查询真实遥测数据
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceMetricHandler implements IntentHandler {

    private final DeviceClient deviceClient;

    @Override
    public IntentEnum getIntent() {
        return IntentEnum.QUERY_DEVICE_METRIC;
    }

    @Override
    public BizQueryResult handle(List<EntityResult> entities) {
        String deviceId = IntentRouter.requireEntity(entities, EntityTypeEnum.DEVICE_ID, "设备编码");
        String metricName = IntentRouter.extractEntity(entities, EntityTypeEnum.METRIC);

        // 先查设备基本信息
        Result<Map<String, Object>> deviceResult = deviceClient.getByCode(deviceId);
        Map<String, Object> deviceData = deviceResult.getData();

        Map<String, Object> data = new HashMap<>(deviceData);

        // TODO: 接入 TelemetryFeignClient 查询 TimescaleDB 遥测数据
        // 当前使用模拟指标数据
        if (metricName != null) {
            data.put("metric", metricName);
            data.put("currentValue", "42.5°C");
            data.put("minValue", "38.2°C");
            data.put("maxValue", "45.1°C");
            data.put("avgValue", "41.3°C");
            data.put("unit", "°C");
            data.put("trend", "正常");
        } else {
            data.put("temperature", "42.5°C");
            data.put("battery", "85%");
            data.put("speed", "1.2 m/s");
            data.put("vibration", "0.15");
            data.put("cpuUsage", "32%");
            data.put("memoryUsage", "45%");
        }

        log.info("设备指标查询完成, deviceId={}, metric={}", deviceId, metricName);

        return BizQueryResult.builder()
                .title("设备指标")
                .data(data)
                .build();
    }
}
