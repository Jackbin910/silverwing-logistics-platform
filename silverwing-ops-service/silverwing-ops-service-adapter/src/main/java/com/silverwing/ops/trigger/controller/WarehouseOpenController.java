package com.silverwing.ops.trigger.controller;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import com.silverwing.common.annotation.SkipAuth;
import com.silverwing.common.domain.Result;
import com.silverwing.ops.application.service.WarehouseOpenAppService;
import com.silverwing.ops.client.OpenWarehouseRequest;
import com.silverwing.ops.client.OpenWarehouseResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

/**
 * 仓储开门控制器
 * 对外暴露 H800 接驳仓开门接口
 * <p>
 * 该接口为内部服务间调用（仅 ai-service 经服务发现调用，网关未对外暴露 /h800），
 * 鉴权已在 ai-service 边缘侧完成，故标记 {@link SkipAuth} 免重复登录校验，
 * 避免内部 Feign 调用因令牌无法透传/校验而 401。
 * </p>
 */
@RestController
@RequestMapping("/h800")
@SkipAuth
@RequiredArgsConstructor
public class WarehouseOpenController {

    private final WarehouseOpenAppService warehouseOpenAppService;

    /**
     * 打开指定库位的 H800 仓储接驳门
     * <p>下发指令后异步返回：HTTP 连接保持挂起，待 H800 返回开门成败后再将真实结果返回前端。</p>
     *
     * @param request 开门请求（含库位编号）
     * @return 挂起的异步结果，成败到达后返回
     */
    @PostMapping("/open")
    public DeferredResult<Result<OpenWarehouseResult>> openWarehouse(@RequestBody OpenWarehouseRequest request) {
        String requestId = request.getRequestId();
        if (CharSequenceUtil.isBlank(requestId)) {
            requestId = IdUtil.fastUUID();
        }
        return warehouseOpenAppService.openWarehouse(request.getLocation(), requestId);
    }
}
