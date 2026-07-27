package com.silverwing.biz.ops.infrastructure.adapter.h800.executor;

import cn.hutool.json.JSONUtil;
import com.silverwing.biz.ops.infrastructure.adapter.h800.api.H800ConverterServiceApi;
import com.silverwing.biz.ops.infrastructure.adapter.h800.dto.H800CommonResult;
import com.silverwing.biz.ops.infrastructure.adapter.h800.dto.H800OpenWareInputDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 设备动作执行器
 * 直接驱动 H800 转换服务开门，本期不引入队列/机器人映射，简化链路
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
     * 打开指定库位的 H800 接驳仓
     *
     * @param location  库位编号，例如 12-A-01
     * @param requestId 请求唯一标识
     */
    public void openWarehouse(String location, String requestId) {
        H800OpenWareInputDTO dto = new H800OpenWareInputDTO();
        dto.setLocation(location);
        dto.setRequestId(requestId);
        // 开门场景不需要出口编号，置空保留以兼容后续接口
        dto.setExitNum(null);
        H800CommonResult<String> result = h800Api.openH800WarehouseDoor(dto);
        // 失败时抛出异常，由应用层统一转换为友好错误
        if (result == null || !Objects.equals(result.getCode(), SUCCESS_CODE)) {
            log.error("H800 开门失败: location={}, result={}", location, JSONUtil.toJsonStr(result));
            throw new RuntimeException(result != null ? result.getMsg() : "H800 无响应");
        }
        log.info("H800 开门指令已发送: location={}, requestId={}", location, requestId);
    }
}
