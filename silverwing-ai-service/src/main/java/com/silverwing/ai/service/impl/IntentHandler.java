package com.silverwing.ai.service.impl;

import com.silverwing.ai.domain.dto.EntityResult;
import com.silverwing.ai.domain.dto.BizQueryResult;
import com.silverwing.ai.domain.enums.IntentEnum;

import java.util.List;

/**
 * 意图处理策略接口
 * 每种意图对应一个实现类，负责具体的业务数据查询
 *
 * <p>
 * 实现此接口并添加 @Component 注解即可自动注册到 IntentRouter
 * </p>
 */
public interface IntentHandler {

    /**
     * 获取该处理器能处理的意图类型
     *
     * @return 意图枚举
     */
    IntentEnum getIntent();

    /**
     * 根据识别出的实体执行业务查询
     *
     * @param entities NLP 提取出的实体列表
     * @return 结构化业务查询结果
     */
    BizQueryResult handle(List<EntityResult> entities);

    /**
     * 根据原始消息和识别出的实体执行业务查询（默认实现委托给handle方法）
     * 子类可重写此方法以支持更复杂的处理逻辑（如NL2SQL降级）
     *
     * @param originalMessage 用户原始消息
     * @param entities        NLP 提取出的实体列表
     * @return 结构化业务查询结果
     */
    default BizQueryResult handle(String originalMessage, List<EntityResult> entities) {
        return handle(entities);
    }
}
