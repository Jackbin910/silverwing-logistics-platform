package com.silverwing.ops.application.service;

import com.silverwing.biz.ops.infrastructure.adapter.h800.executor.DeviceActionExecutor;
import com.silverwing.common.domain.Result;
import com.silverwing.ops.client.OpenWarehouseResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;

/**
 * 仓储开门应用服务
 * 负责生成请求号、编排设备动作执行，并将 H800 返回的开门成败翻译为统一返回结果。
 * <p>采用 Spring {@link DeferredResult} 实现异步返回：下发指令不阻塞业务线程，
 * HTTP 连接保持挂起，待 H800 返回真实成败后再把结果写回前端。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WarehouseOpenAppService {

    /**
     * 开门结果等待上限（毫秒），超时则返回"处理中，请稍后查询状态"
     */
    private static final long OPEN_TIMEOUT_MS = 60_000L;

    private final DeviceActionExecutor deviceActionExecutor;

    /**
     * 打开指定库位的 H800 仓储接驳门（异步返回真实成败）
     *
     * @param location 库位编号，例如 12-A-01
     * @return 挂起的异步结果，H800 返回成败后写回
     */
    public DeferredResult<Result<OpenWarehouseResult>> openWarehouse(String location, String requestId) {
        DeferredResult<Result<OpenWarehouseResult>> deferred = new DeferredResult<>(OPEN_TIMEOUT_MS,
                Result.fail("开门处理超时，请稍后查询状态"));

        deviceActionExecutor.openWarehouse(location, requestId, (ok, msg) -> {
            if (Boolean.TRUE.equals(ok)) {
                setDeferredResult(deferred, requestId,
                        Result.success(new OpenWarehouseResult(true, requestId, "开门成功")));
            } else {
                setDeferredResult(deferred, requestId, Result.fail("开门失败：" + msg));
            }
        });
        return deferred;
    }

    /**
     * 安全写入 DeferredResult 结果
     * <p>超时或已写入后再次 set 会抛 {@link IllegalStateException}，此处忽略并记日志。</p>
     *
     * @param deferred  异步结果
     * @param requestId 请求唯一标识（用于日志追踪）
     * @param result    待返回结果
     */
    private void setDeferredResult(DeferredResult<Result<OpenWarehouseResult>> deferred,
                                   String requestId, Result<OpenWarehouseResult> result) {
        try {
            deferred.setResult(result);
        } catch (IllegalStateException e) {
            log.warn("开门结果写入 DeferredResult 时已过期或已设置，忽略: requestId={}", requestId);
        }
    }
}
