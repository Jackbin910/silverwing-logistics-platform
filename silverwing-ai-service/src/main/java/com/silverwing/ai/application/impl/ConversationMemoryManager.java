package com.silverwing.ai.application.impl;


import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * 对话记忆管理器
 * 按会话ID隔离对话历史，支持多轮对话上下文
 * 使用 Caffeine 本地缓存 + LangChain4j 的 InMemoryChatMemoryStore
 */
@Slf4j
@Service
public class ConversationMemoryManager {

    /**
     * 最大保留消息数（20条 = 约10轮对话）
     */
    private static final int MAX_MESSAGES = 20;

    /**
     * 会话过期时间（30分钟无活动后清除）
     */
    private static final Duration EXPIRE_AFTER_ACCESS = Duration.ofMinutes(30);

    /**
     * 最大并发会话数
     */
    private static final int MAX_SESSIONS = 1000;

    /**
     * LangChain4j 内置的内存记忆存储
     */
    private final ChatMemoryStore memoryStore = new InMemoryChatMemoryStore();

    /**
     * 会话ID → 记忆ID 的映射缓存
     * 用于判断会话是否已存在，防止重复创建
     */
    private final Cache<String, String> sessionCache = Caffeine.newBuilder()
            .maximumSize(MAX_SESSIONS)
            .expireAfterAccess(EXPIRE_AFTER_ACCESS)
            .build();

    /**
     * 获取指定会话的历史消息
     *
     * @param sessionId 会话ID，null 或空字符串则返回空列表
     * @return 该会话的历史消息列表
     */
    public List<ChatMessage> getHistory(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return List.of();
        }

        String memoryId = sessionCache.get(sessionId, k -> {
            log.debug("创建新会话记忆: sessionId={}", k);
            return k;
        });

        List<ChatMessage> messages = memoryStore.getMessages(memoryId);
        log.debug("获取会话历史: sessionId={}, 消息数={}", sessionId, messages.size());
        return messages;
    }

    /**
     * 往指定会话追加消息（用户消息 + AI回复）
     * 超出最大轮数时自动裁剪保留最近的消息
     *
     * @param sessionId 会话ID
     * @param messages  要追加的消息列表
     */
    public void appendMessages(String sessionId, List<ChatMessage> messages) {
        if (sessionId == null || sessionId.isBlank() || messages == null || messages.isEmpty()) {
            return;
        }

        String memoryId = sessionCache.get(sessionId, k -> k);

        // ChatMemoryStore 只有 updateMessages（全量替换），没有 addMessages
        // 先取出现有消息，追加新消息，再写回
        List<ChatMessage> existing = memoryStore.getMessages(memoryId);
        List<ChatMessage> allMessages = new ArrayList<>(existing);
        allMessages.addAll(messages);

        // 超出最大消息数时裁剪到最近 MAX_MESSAGES 条
        if (allMessages.size() > MAX_MESSAGES) {
            allMessages = allMessages.subList(
                    allMessages.size() - MAX_MESSAGES, allMessages.size());
            log.debug("裁剪会话历史: sessionId={}, 保留最近{}条", sessionId, MAX_MESSAGES);
        }

        memoryStore.updateMessages(memoryId, allMessages);
        log.debug("追加会话消息: sessionId={}, 新增{}条, 总{}条",
                sessionId, messages.size(), allMessages.size());
    }

    /**
     * 清除指定会话的所有记忆
     *
     * @param sessionId 会话ID
     */
    public void clear(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return;
        }

        String memoryId = sessionCache.getIfPresent(sessionId);
        if (memoryId != null) {
            memoryStore.deleteMessages(memoryId);
            sessionCache.invalidate(sessionId);
            log.info("清除会话记忆: sessionId={}", sessionId);
        }
    }
}
