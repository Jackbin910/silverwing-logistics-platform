package com.silverwing.biz.ops.infrastructure.adapter.h800.api;

import com.dtflys.forest.annotation.Address;
import com.dtflys.forest.annotation.JSONBody;
import com.dtflys.forest.annotation.Post;
import com.dtflys.forest.callback.OnError;
import com.dtflys.forest.callback.OnSuccess;
import com.silverwing.biz.ops.infrastructure.adapter.h800.dto.H800CommonResult;
import com.silverwing.biz.ops.infrastructure.adapter.h800.dto.H800OpenWareInputDTO;

/**
 * H800 转换服务 Forest HTTP 客户端
 * 仅保留本期所需的开门接口，根地址由 {@link H800AddressSource} 从配置项
 * h800.converter.url 动态提供（形如 192.168.31.81:8081）。
 * <p>
 * 说明：开门为设备异步动作，H800 不会立即返回业务结果，
 * 因此采用 {@link OnSuccess}/{@link OnError} 回调的异步模式（fire-and-forget）：
 * 发送指令即返回，响应与异常都在 Forest 后台线程通过回调处理，
 * 业务线程不被读超时阻塞。
 * </p>
 */
@Address(source = H800AddressSource.class)
public interface H800ConverterServiceApi {

    /**
     * 异步打开指定库位的 H800 接驳仓
     *
     * @param inputDTO 开门入参（含库位与请求号）
     * @param onSuccess 成功回调（收到 H800 响应后触发，data 为反序列化结果）
     * @param onError   异常回调（连接失败、超时等异常触发）
     */
    @Post("/api/base/h800/open")
    void openH800WarehouseDoor(@JSONBody H800OpenWareInputDTO inputDTO,
                               OnSuccess<H800CommonResult<String>> onSuccess,
                               OnError onError);
}
