package com.silverwing.ai.service.tool;

import com.silverwing.ai.client.WorkOrderClient;
import com.silverwing.common.domain.Result;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.P;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 物流平台工单工具类
 * 通过 @Tool 注解将 Java 方法暴露给 LLM 调用
 */
@Slf4j
@RequiredArgsConstructor
public class WorkOrderTools {

    private final WorkOrderClient workOrderClient;

    /**
     * 根据工单号查询工单详情
     */
    @Tool("根据工单号查询工单详情，包括工单类型、状态、处理人、创建时间等")
    public String getWorkOrderByNo(
            @P("工单的唯一编号，例如 WO202401150001") String workOrderNo) {
        try {
            Result<Map<String, Object>> result = workOrderClient.getByNo(workOrderNo);
            if (result != null && result.isSuccess() && result.getData() != null) {
                return formatWorkOrderInfo(result.getData());
            }
            return "未找到工单号为【" + workOrderNo + "】的工单信息";
        } catch (Exception e) {
            log.error("查询工单详情失败: workOrderNo={}", workOrderNo, e);
            return "查询工单详情失败: " + e.getMessage();
        }
    }

    private String formatWorkOrderInfo(Map<String, Object> data) {
        return String.format(
                "工单信息:\n" +
                "  工单号: %s\n" +
                "  工单类型: %s\n" +
                "  工单状态: %s\n" +
                "  处理人: %s\n" +
                "  描述: %s\n" +
                "  创建时间: %s",
                data.getOrDefault("workOrderNo", "未知"),
                data.getOrDefault("workOrderType", "未知"),
                data.getOrDefault("status", "未知"),
                data.getOrDefault("handler", "未知"),
                data.getOrDefault("description", "未知"),
                data.getOrDefault("createTime", "未知")
        );
    }
}
