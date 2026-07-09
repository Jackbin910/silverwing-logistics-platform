package com.silverwing.ai.application.impl.handler;

import com.silverwing.ai.application.dto.BizQueryResult;
import com.silverwing.ai.application.dto.EntityResult;
import com.silverwing.biz.ai.domain.enums.IntentEnum;
import com.silverwing.ai.application.impl.IntentHandler;
import com.silverwing.ai.application.rag.DatabaseRagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据库查询处理器（NL2SQL）
 * 意图：DATABASE_QUERY / DATA_STATISTICS
 * 将用户自然语言问题转换为SQL查询，执行后返回结果
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseQueryHandler implements IntentHandler {

    private final DatabaseRagService databaseRagService;

    @Override
    public IntentEnum getIntent() {
        return IntentEnum.DATABASE_QUERY;
    }

    @Override
    public BizQueryResult handle(List<EntityResult> entities) {
        // 标准实体路由模式下的降级处理：需要原始消息才能执行NL2SQL
        log.warn("DatabaseQueryHandler 被无原始消息调用，返回空结果");
        return BizQueryResult.builder()
                .title("数据库查询")
                .data(Map.of("message", "请提供具体的查询问题"))
                .build();
    }

    @Override
    public BizQueryResult handle(String originalMessage, List<EntityResult> entities) {
        if (originalMessage == null || originalMessage.isBlank()) {
            return handle(entities);
        }

        log.info("开始NL2SQL查询: {}", originalMessage);

        try {
            String answer = databaseRagService.query(originalMessage);

            Map<String, Object> data = new HashMap<>();
            data.put("answer", answer);
            data.put("question", originalMessage);

            return BizQueryResult.builder()
                    .title("数据库查询")
                    .data(data)
                    .build();
        } catch (Exception e) {
            log.error("NL2SQL查询失败: {}", originalMessage, e);
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("error", "查询处理异常: " + e.getMessage());
            return BizQueryResult.builder()
                    .title("数据库查询")
                    .data(errorData)
                    .build();
        }
    }
}
