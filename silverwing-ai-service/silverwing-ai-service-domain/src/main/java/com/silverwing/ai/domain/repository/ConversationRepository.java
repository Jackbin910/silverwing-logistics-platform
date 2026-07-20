package com.silverwing.ai.domain.repository;

import dev.langchain4j.data.message.ChatMessage;

import java.util.List;

/**
 * 对话记忆仓储端口
 * <p>按会话ID隔离多轮对话历史，由基础设施层实现（Redis / Caffeine 等）。
 * 应用层仅依赖此端口，不感知具体的记忆存储技术。</p>
 */
public interface ConversationRepository {

    /**
     * 追加对话消息到指定会话
     *
     * @param sessionId 会话ID
     * @param messages  待追加的消息列表（用户消息 + AI回答）
     */
    void appendMessages(String sessionId, List<ChatMessage> messages);

    /**
     * 获取指定会话的历史对话记录
     *
     * @param sessionId 会话ID
     * @return 历史消息列表，会话为空或不存在时返回空列表
     */
    List<ChatMessage> getHistory(String sessionId);

    /**
     * 清除指定会话的历史记忆（重置对话）
     *
     * @param sessionId 会话ID
     */
    void clear(String sessionId);
}
