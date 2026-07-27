package com.silverwing.biz.ops.infrastructure.adapter.h800.api;

import com.dtflys.forest.annotation.Address;
import com.dtflys.forest.annotation.JSONBody;
import com.dtflys.forest.annotation.Post;
import com.silverwing.biz.ops.infrastructure.adapter.h800.dto.H800CommonResult;
import com.silverwing.biz.ops.infrastructure.adapter.h800.dto.H800OpenWareInputDTO;

/**
 * H800 转换服务 Forest HTTP 客户端
 * 仅保留本期所需的开门接口，地址由配置项 h800.converter.host/port 注入
 */
@Address(host = "${h800.converter.host}", port = "${h800.converter.port}")
public interface H800ConverterServiceApi {

    /**
     * 打开指定库位的 H800 接驳仓
     *
     * @param inputDTO 开门入参（含库位与请求号）
     * @return H800 转换服务返回结果
     */
    @Post("/api/base/h800/open")
    H800CommonResult<String> openH800WarehouseDoor(@JSONBody H800OpenWareInputDTO inputDTO);
}
