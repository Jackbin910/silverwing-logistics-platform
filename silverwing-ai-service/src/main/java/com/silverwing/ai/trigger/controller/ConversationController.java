package com.silverwing.ai.trigger.controller;

import com.silverwing.common.domain.Result;
import com.silverwing.ai.application.dto.ConversationResponse;
import com.silverwing.ai.application.dto.ChatRequest;
import com.silverwing.ai.application.impl.ConversationOrchestrator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 对话 Controller
 * 提供自然语言对话接口，面向前端和语音助手
 * 支持多轮对话：前端传入 sessionId，AI 会记住上下文
 */
@Tag(name = "智能对话", description = "自然语言交互、语音助手入口")
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationOrchestrator orchestrator;

    /**
     * 智能对话（支持多轮对话上下文）
     * 前端首次调用可不传 sessionId，响应中会返回一个新的 sessionId
     * 后续调用传入该 sessionId，AI 会记住之前的对话内容
     */
    @Operation(summary = "智能对话", description = "自然语言交互入口，支持多轮对话上下文")
    @PostMapping
    public Result<ConversationResponse> chat(@Valid @RequestBody ChatRequest request) {
        // sessionId 为空时自动生成一个
        String sessionId = request.getSessionId();
        if (sessionId == null || sessionId.isBlank()) {
            sessionId = java.util.UUID.randomUUID().toString().replace("-", "");
        }

        ConversationResponse response = orchestrator.chat(request.getMessage(), sessionId);
        return Result.success(response);
    }

    /**
     * 清除对话记忆
     * 前端可以主动清除对话历史，重新开始对话
     */
    @Operation(summary = "清除对话记忆", description = "清除指定会话的对话历史，重新开始")
    @DeleteMapping("/memory/{sessionId}")
    public Result<Void> clearMemory(@PathVariable String sessionId) {
        orchestrator.clearMemory(sessionId);
        return Result.success();
    }
}
