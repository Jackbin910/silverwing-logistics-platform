package com.silverwing.ai.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AI会话消息领域模型（子表）
 * <p>
 * 一行代表一条消息（user 提问或 assistant 回复），归属某个 {@link ConversationRecord}。
 * </p>
 *
 * @author silverwing
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationMessage {

    /**
     * 消息ID
     */
    private Long id;

    /**
     * 会话ID
     */
    private String conversationId;

    /**
     * 用户ID（冗余，便于越权校验）
     */
    private Long userId;

    /**
     * 消息角色：user / assistant
     */
    private String role;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 识别的意图
     */
    private String intent;

    /**
     * 提取的实体（JSON字符串）
     */
    private String entities;

    /**
     * 使用的token数量
     */
    private Integer tokenCount;

    /**
     * 响应时间（毫秒）
     */
    private Integer responseTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
