package com.silverwing.ai.application.impl.handler;

import com.silverwing.ai.client.DeviceClient;
import com.silverwing.ai.client.WorkOrderClient;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 故障报告处理器
 * 意图：FAULT_REPORT
 *
 * <p>
 * 查询设备信息，然后通过 Feign 创建维修工单
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FaultReportHandler implements IntentHandler {

    private final DeviceClient deviceClient;
    private final WorkOrderClient workOrderClient;

    @Override
    public IntentEnum getIntent() {
        return IntentEnum.FAULT_REPORT;
    }

    @Override
    public BizQueryResult handle(List<EntityResult> entities) {
        String deviceId = IntentRouter.requireEntity(entities, EntityTypeEnum.DEVICE_ID, "设备编码");
        String faultDesc = IntentRouter.extractEntity(entities, EntityTypeEnum.FAULT_DESC);

        if (faultDesc == null || faultDesc.isBlank()) {
            faultDesc = "用户报告设备故障";
        }

        // 查询设备信息
        Result<Map<String, Object>> deviceResult = deviceClient.getByCode(deviceId);
        Map<String, Object> deviceData = deviceResult.getData();

        // 创建维修工单
        Map<String, Object> params = new HashMap<>();
        params.put("deviceCode", deviceId);
        params.put("type", "repair");
        params.put("description", faultDesc);
        if (deviceData != null && deviceData.containsKey("warehouseId")) {
            params.put("warehouseId", deviceData.get("warehouseId"));
        }

        Result<Map<String, Object>> orderResult = workOrderClient.create(params);
        Map<String, Object> orderData = orderResult.getData();

        String workOrderNo = orderData != null ? (String) orderData.get("workOrderNo") : "未知";
        log.info("故障报告已创建工单, deviceId={}, workOrderNo={}, desc={}", deviceId, workOrderNo, faultDesc);

        Map<String, Object> data = new HashMap<>();
        data.put("workOrderNo", workOrderNo);
        data.put("deviceId", deviceId);
        data.put("faultDesc", faultDesc);
        if (orderData != null) {
            data.putAll(orderData);
        }

        return BizQueryResult.builder()
                .title("故障报告")
                .data(data)
                .message("已为 " + deviceId + " 创建维修工单 " + workOrderNo)
                .build();
    }
}
