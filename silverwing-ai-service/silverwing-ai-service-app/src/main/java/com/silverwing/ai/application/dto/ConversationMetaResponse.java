package com.silverwing.ai.application.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会话列表项响应
 *
 * @author silverwing
 */
@Data
public class ConversationMetaResponse {

    /**
     * 会话ID
     */
    private String conversationId;

    /**
     * 会话标题
     */
    private String title;

    /**
     * 最后一条用户消息预览
     */
    private String lastMessage;

    /**
     * 会话类型
     */
    private String sessionType;

    /**
     * 消息条数
     */
    private Integer messageCount;

    /**
     * 最后更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
