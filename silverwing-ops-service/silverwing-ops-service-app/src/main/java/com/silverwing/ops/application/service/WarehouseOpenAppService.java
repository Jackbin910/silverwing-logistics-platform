package com.silverwing.ops.application.service;

import cn.hutool.core.util.IdUtil;
import com.silverwing.biz.ops.infrastructure.adapter.h800.executor.DeviceActionExecutor;
import com.silverwing.common.domain.Result;
import com.silverwing.ops.client.OpenWarehouseResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 仓储开门应用服务
 * 负责生成请求号、编排设备动作执行，并将异常转换为统一返回结果
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WarehouseOpenAppService {

    private final DeviceActionExecutor deviceActionExecutor;

    /**
     * 打开指定库位的 H800 仓储接驳门
     *
     * @param location 库位编号，例如 12-A-01
     * @return 统一返回结果，含请求号与提示信息
     */
    public Result<OpenWarehouseResult> openWarehouse(String location) {
        // 生成全局唯一请求号，便于跨服务日志串联与幂等追踪
        String requestId = IdUtil.fastUUID();
        try {
            deviceActionExecutor.openWarehouse(location, requestId);
            OpenWarehouseResult result = new OpenWarehouseResult(true, requestId, "开门指令已发送");
            return Result.success(result);
        } catch (Exception e) {
            // TODO-i18n: 错误文案接入 MessageSource 实现国际化
            log.error("开门失败: location={}, requestId={}", location, requestId, e);
            return Result.fail("开门失败：" + e.getMessage());
        }
    }
}
