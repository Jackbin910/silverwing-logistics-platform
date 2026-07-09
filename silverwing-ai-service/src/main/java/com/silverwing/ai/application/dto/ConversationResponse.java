package com.silverwing.ai.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 对话响应 DTO
 * 包含会话ID、NLP 解析结果、业务查询结果和自然语言回答
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationResponse {

    /**
     * 会话ID，前端下次请求时传回来即可实现多轮对话
     */
    private String sessionId;

    /**
     * 识别出的意图
     */
    private String intent;

    /**
     * 提取出的实体列表
     */
    private List<EntityResult> entities;

    /**
     * 业务查询的原始数据
     */
    private Map<String, Object> queryResult;

    /**
     * 自然语言回答
     */
    private String answer;
}
