package com.silverwing.ai.application.history;

import cn.dev33.satoken.stp.StpUtil;
import com.silverwing.ai.domain.repository.ConversationHistoryRepository;
import com.silverwing.ai.domain.repository.ConversationRepository;
import com.silverwing.common.domain.ResultCode;
import com.silverwing.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 对话历史命令服务
 * <p>提供删除会话（同时清理 Redis 记忆）与重命名能力，并做会话归属越权校验。</p>
 *
 * @author silverwing
 */
@Slf4j
@Service
public class ConversationHistoryCommandService {

    private final ConversationHistoryRepository historyRepository;
    private final ConversationRepository conversationRepository;

    public ConversationHistoryCommandService(ConversationHistoryRepository historyRepository,
                                             ConversationRepository conversationRepository) {
        this.historyRepository = historyRepository;
        this.conversationRepository = conversationRepository;
    }

    /**
     * 删除指定会话：逻辑删除 MySQL 记录并清理 Redis 对话记忆
     *
     * @param conversationId 会话ID
     */
    public void delete(String conversationId) {
        Long userId = StpUtil.getLoginIdAsLong();
        assertOwner(conversationId, userId);
        historyRepository.deleteByConversationId(conversationId, userId);
        conversationRepository.clear(conversationId);
    }

    /**
     * 重命名指定会话
     *
     * @param conversationId 会话ID
     * @param title          新标题
     */
    public void rename(String conversationId, String title) {
        Long userId = StpUtil.getLoginIdAsLong();
        assertOwner(conversationId, userId);
        historyRepository.rename(conversationId, userId, title);
    }

    /**
     * 校验会话归属当前用户，否则抛越权异常
     *
     * @param conversationId 会话ID
     * @param userId         当前用户ID
     */
    private void assertOwner(String conversationId, Long userId) {
        Long ownerId = historyRepository.selectUserId(conversationId);
        if (ownerId == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "ai.conversation.not.found");
        }
        if (!userId.equals(ownerId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "ai.conversation.forbidden");
        }
    }
}
