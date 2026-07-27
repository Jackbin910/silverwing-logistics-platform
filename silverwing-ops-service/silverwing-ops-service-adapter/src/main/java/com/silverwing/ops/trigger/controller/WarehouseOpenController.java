package com.silverwing.ops.trigger.controller;

import com.silverwing.common.domain.Result;
import com.silverwing.ops.application.service.WarehouseOpenAppService;
import com.silverwing.ops.client.OpenWarehouseRequest;
import com.silverwing.ops.client.OpenWarehouseResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 仓储开门控制器
 * 对外暴露 H800 接驳仓开门接口
 */
@RestController
@RequestMapping("/api/ops/warehouse")
@RequiredArgsConstructor
public class WarehouseOpenController {

    private final WarehouseOpenAppService warehouseOpenAppService;

    /**
     * 打开指定库位的 H800 仓储接驳门
     *
     * @param request 开门请求（含库位编号）
     * @return 统一返回结果
     */
    @PostMapping("/open")
    public Result<OpenWarehouseResult> openWarehouse(@RequestBody OpenWarehouseRequest request) {
        return warehouseOpenAppService.openWarehouse(request.getLocation());
    }
}
