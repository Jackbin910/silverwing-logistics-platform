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
 * 鉴权说明（采用「网关统一鉴权 + 服务免校验」模式）：
 * <ul>
 *   <li>直接请求（外部经网关 {@code /ops/h800/open}）：由网关 SaReactorFilter 统一做登录校验，
 *       未登录返回 401，本服务不再重复校验（标记 {@link SkipAuth}）</li>
 *   <li>ai-service 大模型工具经 Feign 内部调用：绕过网关、无用户令牌，
 *       依托 AI 模块边缘（网关 {@code /ai/**}）已完成用户鉴权，本服务免登录放行</li>
 * </ul>
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
