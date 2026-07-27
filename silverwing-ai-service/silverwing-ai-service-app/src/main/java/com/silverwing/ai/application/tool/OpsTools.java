package com.silverwing.ai.application.tool;

import cn.hutool.core.util.IdUtil;
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
     * 当用户表达"打开/开一下 H800 某库位的门""开门""放料/取料"等意图时由 LLM 调用本工具。
     * <p>请求号由系统自动用 hutool 生成，避免 LLM 编造重复或非法值，保证全链路日志可追踪。</p>
     *
     * @param location 库位编号，例如 12-1-1
     * @return 开门结果的自然语言描述（含请求号与成败信息）
     */
    @Tool("打开指定库位的 H800 仓储接驳门（驱动库位开门）。当用户说'开 h800 某库位的门''打开 12-1-1 的库位门''帮我开一下门'等时调用；参数 location 为库位编号，形如 12-1-11")
    public String openWarehouse(@P("库位编号，例如 12-1-1") String location) {
        // 请求号由系统生成，避免 LLM 编造导致重复或非法，便于全链路日志追踪
        String requestId = IdUtil.fastUUID();
        try {
            Result<OpenWarehouseResult> result =
                opsWarehouseClient.openWarehouse(new OpenWarehouseRequest(location, requestId));
            if (result != null && result.isSuccess() && result.getData() != null) {
                return "已向 H800 发送开门指令并收到结果，库位=" + location
                    + "，请求号=" + result.getData().getRequestId()
                    + "，结果=" + result.getMessage();
            }
            return "开门失败：" + (result != null ? result.getMessage() : "未知错误");
        } catch (Exception e) {
            log.error("调用 ops-service 开门失败: location={}, requestId={}", location, requestId, e);
            return "开门调用失败：" + e.getMessage();
        }
    }
}
