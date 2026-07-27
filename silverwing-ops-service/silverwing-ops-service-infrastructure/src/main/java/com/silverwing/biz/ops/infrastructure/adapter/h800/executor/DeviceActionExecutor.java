package com.silverwing.biz.ops.infrastructure.adapter.h800.executor;

import com.dtflys.forest.callback.OnError;
import com.dtflys.forest.callback.OnSuccess;
import com.silverwing.biz.ops.infrastructure.adapter.h800.api.H800ConverterServiceApi;
import com.silverwing.biz.ops.infrastructure.adapter.h800.dto.H800CommonResult;
import com.silverwing.biz.ops.infrastructure.adapter.h800.dto.H800OpenWareInputDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * 设备动作执行器
 * 驱动 H800 转换服务开门，本期不引入队列/机器人映射，简化链路。
 * <p>开门为设备异步动作，本执行器以 fire-and-forget 方式发送指令（不阻塞调用方），
 * H800 返回开门成败后通过 {@code callback} 通知上层，由上层翻译为对外结果。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceActionExecutor {

    /**
     * H800 转换服务成功状态码
     */
    private static final Integer SUCCESS_CODE = 1;

    private final H800ConverterServiceApi h800Api;

    /**
     * 异步打开指定库位的 H800 接驳仓
     * <p>发送指令即提交给 Forest 异步线程，不阻塞调用方；H800 返回开门成败后
     * 通过 callback 通知上层（不抛异常，避免影响已下发的指令）。</p>
     *
     * @param location  库位编号，例如 12-A-01
     * @param requestId 请求唯一标识
     * @param callback  成败回调，(是否成功, 描述) 由上层翻译为对外结果
     */
    public void openWarehouse(String location, String requestId, BiConsumer<Boolean, String> callback) {
        H800OpenWareInputDTO dto = new H800OpenWareInputDTO();
        dto.setLocation(location);
        dto.setRequestId(requestId);
        // 开门场景不需要出口编号，置空保留以兼容后续接口
        dto.setExitNum(null);

        // 成功回调：H800 返回响应后触发，data 为反序列化后的通用结果
        OnSuccess<H800CommonResult<String>> onSuccess = (data, request, response) -> {
            boolean ok = data != null && Objects.equals(data.getCode(), SUCCESS_CODE);
            String msg = data != null ? data.getMsg() : "无响应";
            callback.accept(ok, msg);
        };

        // 异常回调：连接失败、超时等触发，同样通过 callback 通知上层
        OnError onError = (ex, request, response) -> {
            log.error("H800 开门调用异常: location={}, requestId={}", location, requestId, ex);
            callback.accept(false, "调用异常：" + ex.getMessage());
        };

        h800Api.openH800WarehouseDoor(dto, onSuccess, onError);
        log.info("H800 开门指令已发送: location={}, requestId={}", location, requestId);
    }
}
