package com.silverwing.ai.application.tool;

import com.silverwing.common.domain.Result;
import com.silverwing.ops.client.OpenWarehouseRequest;
import com.silverwing.ops.client.OpenWarehouseResult;
import com.silverwing.ops.client.OpsWarehouseClient;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 运维服务工具类
 * 通过 @Tool 将 H800 开门能力暴露给 LLM 调用
 */
@Slf4j
@RequiredArgsConstructor
public class OpsTools {

    private final OpsWarehouseClient opsWarehouseClient;

    /**
     * 打开指定库位的 H800 仓储接驳门
     * 当用户希望打开某个库位的接驳仓门时由 LLM 调用本工具
     *
     * @param location 库位编号，例如 12-A-01
     * @return 开门结果的自然语言描述
     */
    @Tool("打开指定库位的 H800 仓储接驳门（驱动库位）；参数 location 形如 12-1-11")
    public String openWarehouse(@P("库位编号，例如 12-1-1") String location) {
        try {
            Result<OpenWarehouseResult> result =
                opsWarehouseClient.openWarehouse(new OpenWarehouseRequest(location));
            if (result != null && result.isSuccess() && result.getData() != null) {
                return "已向 H800 发送开门指令，库位=" + location
                    + "，请求号=" + result.getData().getRequestId();
            }
            return "开门失败：" + (result != null ? result.getMessage() : "未知错误");
        } catch (Exception e) {
            log.error("调用 ops-service 开门失败: location={}", location, e);
            return "开门调用失败：" + e.getMessage();
        }
    }
}
