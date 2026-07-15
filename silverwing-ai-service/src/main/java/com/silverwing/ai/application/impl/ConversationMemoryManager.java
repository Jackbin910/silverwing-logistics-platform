package com.silverwing.ai.application.impl;


import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 对话记忆管理器
 * 按会话ID隔离对话历史，支持多轮对话上下文
 * 使用 Caffeine 本地缓存 + LangChain4j 的 InMemoryChatMemoryStore
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationMemoryManager {

    /**
     * 最大保留的对话消息条数。
     * 假设 Qwen 模型上下文为 8K，每轮对话(User+Ai)平均 500 Token，
     * 保留 20 条(即 10 轮对话)大约占用 5000 Token，留出足够空间给系统提示词和当前问题。
     */
    private static final int MAX_MESSAGES = 20;



    private final ChatMemoryStore memoryStore;


    /**
     * 获取或创建当前会话的记忆上下文
     */
    private ChatMemory getOrCreateMemory(String sessionId) {
        return MessageWindowChatMemory.builder()
                .id(sessionId)
                .chatMemoryStore(memoryStore)
                .maxMessages(MAX_MESSAGES)
                .build();
    }

    /**
     * 追加对话消息到 Redis，并自动裁剪超出窗口的早期消息
     */
    public void appendMessages(String sessionId, List<ChatMessage> messages) {
        if (sessionId == null || sessionId.isBlank() || messages == null || messages.isEmpty()) {
            return;
        }
        ChatMemory memory = getOrCreateMemory(sessionId);
        messages.forEach(memory::add);
    }

    /**
     * 获取当前会话的历史对话记录
     */
    public List<ChatMessage> getHistory(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return List.of();
        }
        return getOrCreateMemory(sessionId).messages();
    }

    /**
     * 清除当前会话的历史记忆（重置对话）
     */
    public void clear(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return;
        }
        getOrCreateMemory(sessionId).clear();
        log.info("清除会话记忆: sessionId={}", sessionId);
    }
}
