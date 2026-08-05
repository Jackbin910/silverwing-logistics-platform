package com.silverwing.ai.application.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会话消息明细响应
 *
 * @author silverwing
 */
@Data
public class ConversationMessageResponse {

    /**
     * 消息ID
     */
    private Long id;

    /**
     * 会话ID
     */
    private String conversationId;

    /**
     * 角色：user / assistant
     */
    private String role;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 识别意图
     */
    private String intent;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
