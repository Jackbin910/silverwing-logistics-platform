package com.silverwing.ai.domain.repository;

import com.silverwing.ai.domain.model.ConversationMessage;
import com.silverwing.ai.domain.model.ConversationRecord;
import com.silverwing.ai.domain.model.NlpParseResult;

import java.util.List;

/**
 * 会话历史仓储接口
 * <p>
 * 采用父子表结构：{@link ConversationRecord} 对应会话主表，{@link ConversationMessage} 对应消息子表。
 * 仓储负责两者的持久化与查询，聚合根 {@code ConversationRecord} 统领其下的消息明细。
 * </p>
 *
 * @author silverwing
 */
public interface ConversationHistoryRepository {

    /**
     * 保存一轮问答（用户消息 + AI回复），并维护父表会话汇总信息
     *
     * @param conversationId 会话ID
     * @param userId         用户ID
     * @param userMessage    用户提问
     * @param aiMessage      AI回复
     * @param parseResult    NLP解析结果（意图/实体）
     * @param tokenCount     本次token消耗
     * @param responseTime   本次响应耗时（毫秒）
     */
    void saveRound(String conversationId, Long userId, String userMessage, String aiMessage,
                   NlpParseResult parseResult, int tokenCount, int responseTime);

    /**
     * 查询用户会话列表（按最后更新时间倒序）
     *
     * @param userId 用户ID
     * @return 会话列表
     */
    List<ConversationRecord> listConversations(Long userId);

    /**
     * 查询会话详情（含消息明细），并校验归属用户
     *
     * @param conversationId 会话ID
     * @param userId         用户ID
     * @return 会话详情，不存在或无权限返回 null
     */
    ConversationRecord getConversation(String conversationId, Long userId);

    /**
     * 逻辑删除会话（父表与子表均标记 deleted=1）
     *
     * @param conversationId 会话ID
     * @param userId         用户ID（越权校验）
     */
    void deleteByConversationId(String conversationId, Long userId);

    /**
     * 重命名会话（更新父表标题）
     *
     * @param conversationId 会话ID
     * @param userId         用户ID（越权校验）
     * @param title          新标题
     */
    void rename(String conversationId, Long userId, String title);

    /**
     * 查询会话归属用户ID（用于删除时同步清理Redis记忆）
     *
     * @param conversationId 会话ID
     * @return 用户ID，不存在返回 null
     */
    Long selectUserId(String conversationId);
}
