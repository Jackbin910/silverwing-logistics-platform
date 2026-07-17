package com.silverwing.ai.application.impl;

import com.silverwing.ai.domain.model.EntityResult;
import com.silverwing.ai.domain.model.BizQueryResult;
import com.silverwing.biz.ai.domain.enums.EntityTypeEnum;
import com.silverwing.biz.ai.domain.enums.IntentEnum;
import com.silverwing.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 意图路由器
 * 根据 NLP 识别的意图，路由到对应的业务处理器查数据
 *
 * <p>
 * 扩展新意图只需两步：
 * 1. 在 IntentEnum 添加枚举值
 * 2. 实现 IntentHandler 接口并加 @Component 注解
 * </p>
 */
@Slf4j
@Service
public class IntentRouter {

    private final Map<IntentEnum, IntentHandler> handlerMap;

    /**
     * 构造函数，自动收集所有 IntentHandler 实现
     *
     * @param handlers Spring 容器中所有 IntentHandler 实现类
     */
    public IntentRouter(List<IntentHandler> handlers) {
        this.handlerMap = handlers.stream()
                .collect(Collectors.toMap(IntentHandler::getIntent, h -> h, (a, b) -> a));
        log.info("已注册 {} 个意图处理器: {}", handlerMap.size(), handlerMap.keySet());
    }

    /**
     * 根据意图和实体路由到对应的业务处理器
     *
     * @param intent   识别出的意图
     * @param entities 识别出的实体列表
     * @return 业务查询结果
     */
    public BizQueryResult route(IntentEnum intent, List<EntityResult> entities) {
        return route(intent, null, entities);
    }

    /**
     * 根据意图、原始消息和实体路由到对应的业务处理器
     * 支持NL2SQL等需要原始消息的复杂处理场景
     *
     * @param intent          识别出的意图
     * @param originalMessage 用户原始消息（可为null）
     * @param entities        识别出的实体列表
     * @return 业务查询结果
     */
    public BizQueryResult route(IntentEnum intent, String originalMessage, List<EntityResult> entities) {
        IntentHandler handler = handlerMap.get(intent);

        if (handler == null) {
            log.warn("未找到意图处理器: {}", intent);
            throw new BusinessException("暂不支持该意图类型: " + intent.getName());
        }

        log.info("路由意图 [{}] 到处理器 [{}]", intent, handler.getClass().getSimpleName());

        // 优先使用支持原始消息的重载方法
        if (originalMessage != null && !originalMessage.isBlank()) {
            return handler.handle(originalMessage, entities);
        }
        return handler.handle(entities);
    }

    /**
     * 从实体列表中按类型提取第一个匹配值
     *
     * @param entities 实体列表
     * @param type     实体类型
     * @return 实体值，未找到返回 null
     */
    public static String extractEntity(List<EntityResult> entities, EntityTypeEnum type) {
        return entities.stream()
                .filter(e -> e.getType() == type)
                .map(EntityResult::getValue)
                .findFirst()
                .orElse(null);
    }

    /**
     * 从实体列表中按类型提取第一个匹配值，未找到则抛异常
     *
     * @param entities  实体列表
     * @param type      实体类型
     * @param fieldName 字段中文名（用于错误提示）
     * @return 实体值
     */
    public static String requireEntity(List<EntityResult> entities, EntityTypeEnum type, String fieldName) {
        String value = extractEntity(entities, type);
        if (value == null || value.isBlank()) {
            throw new BusinessException("请提供" + fieldName + "信息");
        }
        return value;
    }
}
