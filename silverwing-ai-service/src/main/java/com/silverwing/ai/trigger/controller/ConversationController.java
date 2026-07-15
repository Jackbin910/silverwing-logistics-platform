package com.silverwing.ai.trigger.controller;

import cn.hutool.core.text.CharSequenceUtil;
import com.silverwing.common.annotation.Log;
import com.silverwing.common.domain.Result;
import com.silverwing.ai.application.dto.ConversationResponse;
import com.silverwing.ai.application.dto.ChatRequest;
import com.silverwing.ai.application.impl.ConversationOrchestrator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

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
     * 智能对话（流式响应）
     * 通过 SSE 逐 token 推送回答，前端可实时展示打字效果
     * 支持多轮对话上下文
     */
    @Operation(summary = "智能对话-流式", description = "自然语言交互入口，流式逐 token 推送回答，支持多轮对话上下文")
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chatStream(
            @RequestParam("message") String message,
            @RequestParam(value = "sessionId", required = false) String sessionId) {
        if (CharSequenceUtil.isBlank(message)) {
            return Flux.just(ServerSentEvent.<String>builder().event("error").data("消息内容不能为空").build());
        }

        // sessionId 为空时自动生成一个
        if (sessionId == null || sessionId.isBlank()) {
            sessionId = java.util.UUID.randomUUID().toString().replace("-", "");
        }

        // 返回 sessionId in first event
        return Flux.concat(
                Flux.just(ServerSentEvent.<String>builder().event("sessionId").data(sessionId).build()),
                orchestrator.chatStream(message, sessionId)
                        .map(token -> ServerSentEvent.<String>builder().event("token").data(token).build()),
                Flux.just(ServerSentEvent.<String>builder().event("done").data("[DONE]").build())
        ).onErrorResume(e -> Flux.just(
                ServerSentEvent.<String>builder().event("error").data("处理异常：" + e.getMessage()).build()
        ));
    }

    /**
     * 清除对话记忆
     * 前端可以主动清除对话历史，重新开始对话
     */
    @Log(title = "智能对话-清除对话记忆", businessType = 3)
    @Operation(summary = "清除对话记忆", description = "清除指定会话的对话历史，重新开始")
    @DeleteMapping("/memory/{sessionId}")
    public Result<Void> clearMemory(@PathVariable String sessionId) {
        orchestrator.clearMemory(sessionId);
        return Result.success();
    }
}
