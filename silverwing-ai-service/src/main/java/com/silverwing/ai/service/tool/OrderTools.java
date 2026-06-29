package com.silverwing.ai.service.tool;

import com.silverwing.ai.client.OrderClient;
import com.silverwing.common.domain.Result;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.P;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 物流平台订单工具类
 * 通过 @Tool 注解将 Java 方法暴露给 LLM 调用
 * LLM 会根据用户意图自动选择合适的工具
 */
@Slf4j
@RequiredArgsConstructor
public class OrderTools {

    private final OrderClient orderClient;

    /**
     * 根据订单号查询订单详情
     */
    @Tool("根据订单号查询订单的完整信息，包括订单状态、物流进度、收发货信息等")
    public String getOrderByNo(
            @P("订单的唯一编号，例如 ORD202401150001") String orderNo) {
        try {
            Result<Map<String, Object>> result = orderClient.getByOrderNo(orderNo);
            if (result != null && result.isSuccess() && result.getData() != null) {
                return formatOrderInfo(result.getData());
            }
            return "未找到订单号为【" + orderNo + "】的订单信息";
        } catch (Exception e) {
            log.error("查询订单详情失败: orderNo={}", orderNo, e);
            return "查询订单详情失败: " + e.getMessage();
        }
    }

    /**
     * 根据订单号查询物流状态
     */
    @Tool("查询订单的物流运输状态，包括当前阶段、预计到达时间、当前位置等")
    public String getOrderLogisticsStatus(
            @P("订单的唯一编号") String orderNo) {
        try {
            Result<Map<String, Object>> result = orderClient.getByOrderNo(orderNo);
            if (result != null && result.isSuccess() && result.getData() != null) {
                Map<String, Object> data = result.getData();
                return String.format("订单【%s】物流状态:\n" +
                                "  订单状态: %s\n" +
                                "  当前阶段: %s\n" +
                                "  当前位置: %s\n" +
                                "  预计到达: %s\n" +
                                "  更新时间: %s",
                        orderNo,
                        data.getOrDefault("orderStatus", "未知"),
                        data.getOrDefault("currentStage", "未知"),
                        data.getOrDefault("currentLocation", "未知"),
                        data.getOrDefault("estimatedArrival", "未知"),
                        data.getOrDefault("updateTime", "未知"));
            }
            return "未找到订单【" + orderNo + "】的物流信息";
        } catch (Exception e) {
            log.error("查询物流状态失败: orderNo={}", orderNo, e);
            return "查询物流状态失败: " + e.getMessage();
        }
    }

    private String formatOrderInfo(Map<String, Object> data) {
        return String.format(
                "订单信息:\n" +
                "  订单号: %s\n" +
                "  订单状态: %s\n" +
                "  发货人: %s\n" +
                "  收货人: %s\n" +
                "  发货地址: %s\n" +
                "  收货地址: %s\n" +
                "  货物类型: %s\n" +
                "  创建时间: %s",
                data.getOrDefault("orderNo", "未知"),
                data.getOrDefault("orderStatus", "未知"),
                data.getOrDefault("shipper", "未知"),
                data.getOrDefault("receiver", "未知"),
                data.getOrDefault("shipAddress", "未知"),
                data.getOrDefault("receiveAddress", "未知"),
                data.getOrDefault("cargoType", "未知"),
                data.getOrDefault("createTime", "未知")
        );
    }
}
