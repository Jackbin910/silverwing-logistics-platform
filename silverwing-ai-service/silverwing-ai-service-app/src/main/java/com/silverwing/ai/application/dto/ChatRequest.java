package com.silverwing.ai.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 对话请求 DTO
 */
@Data
public class ChatRequest {

    /**
     * 会话ID，用于多轮对话上下文
     */
    private String sessionId;

    /**
     * 用户输入的自然语言内容
     */
    @NotBlank(message = "{validation.chat.message.notblank}")
    private String message;
}
