package com.silverwing.ai.application.impl.handler;

import com.silverwing.ai.client.OrderClient;
import com.silverwing.ai.domain.model.BizQueryResult;
import com.silverwing.ai.domain.model.EntityResult;
import com.silverwing.biz.ai.domain.enums.EntityTypeEnum;
import com.silverwing.biz.ai.domain.enums.IntentEnum;
import com.silverwing.ai.application.impl.IntentHandler;
import com.silverwing.ai.application.impl.IntentRouter;
import com.silverwing.ai.domain.service.rag.DatabaseRagService;
import com.silverwing.common.domain.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单状态查询处理器
 * 意图：QUERY_ORDER_STATUS
 *
 * <p>
 * 处理策略：
 * 1. 如果用户提供了具体订单号 → 通过 Feign 远程精确查询
 * 2. 如果未提供订单号 → 降级到 NL2SQL 进行自然语言数据库查询
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderStatusHandler implements IntentHandler {

    private final OrderClient orderClient;

    /**
     * NL2SQL 服务（可选注入，用于无订单号时的降级查询）
     */
    @Autowired(required = false)
    private DatabaseRagService databaseRagService;

    @Override
    public IntentEnum getIntent() {
        return IntentEnum.QUERY_ORDER_STATUS;
    }

    @Override
    public BizQueryResult handle(List<EntityResult> entities) {
        // 尝试从实体中提取订单号
        String orderNo = IntentRouter.extractEntity(entities, EntityTypeEnum.ORDER_NO);

        // 有订单号：走精确查询（Feign远程调用）
        if (orderNo != null && !orderNo.isBlank()) {
            return queryByOrderNo(orderNo);
        }

        // 无订单号：返回提示信息，引导用户提供或降级NL2SQL
        log.info("未提取到订单号，尝试NL2SQL降级查询");
        Map<String, Object> hintData = new HashMap<>();
        hintData.put("hint", "未提供具体订单号");
        hintData.put("suggestions", List.of("请提供订单号进行精确查询", "例如：查询订单 ORD-001 的状态"));

        return BizQueryResult.builder()
                .title("订单状态查询")
                .data(hintData)
                .build();
    }

    /**
     * 支持原始消息的重载方法：无订单号时自动降级到 NL2SQL
     */
    @Override
    public BizQueryResult handle(String originalMessage, List<EntityResult> entities) {
        // 尝试从实体中提取订单号
        String orderNo = IntentRouter.extractEntity(entities, EntityTypeEnum.ORDER_NO);

        // 有订单号：走精确查询
        if (orderNo != null && !orderNo.isBlank()) {
            return queryByOrderNo(orderNo);
        }

        // 无订单号但有原始消息 + NL2SQL服务可用：自动降级到数据库查询
        if (originalMessage != null && !originalMessage.isBlank() && databaseRagService != null) {
            log.info("无订单号，降级使用NL2SQL查询: {}", originalMessage);
            try {
                String answer = databaseRagService.query(originalMessage);
                Map<String, Object> data = new HashMap<>();
                data.put("answer", answer);
                data.put("source", "nl2sql");
                return BizQueryResult.builder()
                        .title("订单状态查询")
                        .data(data)
                        .build();
            } catch (Exception e) {
                log.error("NL2SQL降级查询失败", e);
            }
        }

        // 最终兜底：返回提示
        return handle(entities);
    }

    /**
     * 根据订单号精确查询订单状态
     */
    private BizQueryResult queryByOrderNo(String orderNo) {
        Result<Map<String, Object>> result = orderClient.getByOrderNo(orderNo);
        Map<String, Object> data = result.getData();

        log.info("订单状态查询完成, orderNo={}, status={}", orderNo, data.get("status"));

        return BizQueryResult.builder()
                .title("订单状态")
                .data(data)
                .build();
    }
}
