package com.silverwing.ai.application.impl.handler;

import cn.hutool.core.util.IdUtil;
import com.silverwing.ai.application.impl.IntentHandler;
import com.silverwing.ai.application.impl.IntentRouter;
import com.silverwing.ai.domain.model.BizQueryResult;
import com.silverwing.ai.domain.model.EntityResult;
import com.silverwing.biz.ai.domain.enums.EntityTypeEnum;
import com.silverwing.biz.ai.domain.enums.IntentEnum;
import com.silverwing.common.domain.Result;
import com.silverwing.common.domain.ResultCode;
import com.silverwing.common.exception.BusinessException;
import com.silverwing.ops.client.OpenWarehouseRequest;
import com.silverwing.ops.client.OpenWarehouseResult;
import com.silverwing.ops.client.OpsWarehouseClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 开门处理器
 * 意图：OPEN_WAREHOUSE
 *
 * <p>识别到用户开门意图后，调用 ops-service 打开指定库位的 H800 接驳仓门，
 * 同步等待设备真实成败（最长约 60 秒）后返回结构化结果，交由编排层转自然语言回复。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OpenWarehouseHandler implements IntentHandler {

    /**
     * 库位编号正则：形如 12-1-11、3-2-5
     */
    private static final Pattern LOCATION_PATTERN = Pattern.compile("\\d{1,3}-\\d{1,3}-\\d{1,3}");

    private final OpsWarehouseClient opsWarehouseClient;

    @Override
    public IntentEnum getIntent() {
        return IntentEnum.OPEN_WAREHOUSE;
    }

    @Override
    public BizQueryResult handle(List<EntityResult> entities) {
        // 未携带原始消息时直接委托含消息的重载，缺失库位由编排层兜底
        return handle(null, entities);
    }

    @Override
    public BizQueryResult handle(String originalMessage, List<EntityResult> entities) {
        // 1. 提取库位编号：优先取 NLP 实体，缺失时从原始消息用正则兜底
        String location = IntentRouter.extractEntity(entities, EntityTypeEnum.BIN_LOCATION);
        if (location == null || location.isBlank()) {
            location = extractLocationByRegex(originalMessage);
        }
        if (location == null || location.isBlank()) {
            throw BusinessException.i18n(ResultCode.INTERNAL_SERVER_ERROR,
                    "ai.intent.field.required", "库位编号");
        }

        // 2. 请求号由系统生成，避免 LLM/用户侧重复或非法，便于全链路日志追踪
        String requestId = IdUtil.fastUUID();

        try {
            Result<OpenWarehouseResult> result =
                    opsWarehouseClient.openWarehouse(new OpenWarehouseRequest(location, requestId));
            boolean success = result != null && result.isSuccess() && result.getData() != null;
            String message = result != null ? result.getMessage() : "未知错误";

            Map<String, Object> data = new HashMap<>(4);
            data.put("location", location);
            data.put("requestId", requestId);
            data.put("success", success);
            data.put("message", message);

            String tip = success
                    ? "已向 H800 发送开门指令并成功开启库位 " + location + " 的仓门"
                    : "开启库位 " + location + " 的仓门失败：" + message;

            log.info("开门处理完成: location={}, requestId={}, success={}", location, requestId, success);
            return BizQueryResult.builder()
                    .title("开门")
                    .data(data)
                    .message(tip)
                    .build();
        } catch (Exception e) {
            log.error("调用 ops-service 开门失败: location={}, requestId={}", location, requestId, e);
            Map<String, Object> data = new HashMap<>(4);
            data.put("location", location);
            data.put("requestId", requestId);
            data.put("success", false);
            data.put("message", e.getMessage());
            return BizQueryResult.builder()
                    .title("开门")
                    .data(data)
                    .message("开门调用失败：" + e.getMessage())
                    .build();
        }
    }

    /**
     * 从原始消息中用正则兜底提取库位编号
     *
     * @param message 用户原始消息
     * @return 库位编号，未匹配返回 null
     */
    private String extractLocationByRegex(String message) {
        if (message == null || message.isBlank()) {
            return null;
        }
        Matcher matcher = LOCATION_PATTERN.matcher(message);
        return matcher.find() ? matcher.group() : null;
    }
}
