package com.silverwing.ai.infrastructure.adapter.conversation;

import com.silverwing.ai.domain.repository.ConversationRepository;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 对话记忆仓储基础设施实现
 * <p>基于 LangChain4j 的 {@link ChatMemoryStore}（Redis / 本地）与
 * {@link MessageWindowChatMemory} 实现多轮对话记忆，按会话ID隔离上下文。</p>
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class ConversationRepositoryImpl implements ConversationRepository {

    /**
     * 最大保留的对话消息条数。
     * 假设 Qwen 模型上下文为 8K，每轮对话(User+Ai)平均 500 Token，
     * 保留 20 条(即 10 轮对话)大约占用 5000 Token，留出足够空间给系统提示词和当前问题。
     */
    private static final int MAX_MESSAGES = 20;

    /**
     * 底层记忆存储（由 LangChain4jConfig 注入，可为 Redis 或本地实现）
     */
    private final ChatMemoryStore memoryStore;

    /**
     * 获取或创建当前会话的记忆上下文
     *
     * @param sessionId 会话ID
     * @return 会话对应的 ChatMemory
     */
    private ChatMemory getOrCreateMemory(String sessionId) {
        return MessageWindowChatMemory.builder()
                .id(sessionId)
                .chatMemoryStore(memoryStore)
                .maxMessages(MAX_MESSAGES)
                .build();
    }

    /**
     * 追加对话消息到存储，并自动裁剪超出窗口的早期消息
     *
     * @param sessionId 会话ID
     * @param messages  待追加的消息列表
     */
    @Override
    public void appendMessages(String sessionId, List<ChatMessage> messages) {
        if (sessionId == null || sessionId.isBlank() || messages == null || messages.isEmpty()) {
            return;
        }
        ChatMemory memory = getOrCreateMemory(sessionId);
        messages.forEach(memory::add);
    }

    /**
     * 获取当前会话的历史对话记录
     *
     * @param sessionId 会话ID
     * @return 历史消息列表
     */
    @Override
    public List<ChatMessage> getHistory(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return List.of();
        }
        return getOrCreateMemory(sessionId).messages();
    }

    /**
     * 清除当前会话的历史记忆（重置对话）
     *
     * @param sessionId 会话ID
     */
    @Override
    public void clear(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return;
        }
        getOrCreateMemory(sessionId).clear();
        log.info("清除会话记忆: sessionId={}", sessionId);
    }
}
