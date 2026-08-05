package com.silverwing.ai.infrastructure.adapter.conversation;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.silverwing.ai.domain.model.ConversationMessage;
import com.silverwing.ai.domain.model.ConversationRecord;
import com.silverwing.ai.domain.model.NlpParseResult;
import com.silverwing.ai.domain.repository.ConversationHistoryRepository;
import com.silverwing.biz.ai.infrastructure.dao.po.AiConversationMessagePO;
import com.silverwing.biz.ai.infrastructure.dao.po.AiConversationPO;
import com.silverwing.biz.ai.infrastructure.mapper.AiConversationMapper;
import com.silverwing.biz.ai.infrastructure.mapper.AiConversationMessageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 会话历史仓储实现（父子表）
 * <p>
 * 父表 {@code ai_conversation} 存会话级字段，子表 {@code ai_conversation_message} 存每轮问答明细。
 * 落库时：首次会话建父表记录，后续会话仅更新父表汇总（lastMessage/messageCount/updateTime）；
 * 每条问答写两条子表消息（user + assistant）。
 * </p>
 *
 * @author silverwing
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class ConversationHistoryRepositoryImpl implements ConversationHistoryRepository {

    private static final int TITLE_MAX_LENGTH = 50;

    private final AiConversationMapper conversationMapper;
    private final AiConversationMessageMapper messageMapper;
    private final ConversationHistoryInfraConvertor convertor;

    /**
     * 保存一轮问答：先写子表两条消息，再维护父表会话汇总
     */
    @Override
    public void saveRound(String conversationId, Long userId, String userMessage, String aiMessage,
                          NlpParseResult parseResult, int tokenCount, int responseTime) {
        // 写入用户消息
        saveMessage(conversationId, userId, "user", userMessage, null, null, 0, 0);
        // 写入AI回复
        String intent = parseResult != null && parseResult.getIntent() != null
                ? parseResult.getIntent().getCode() : null;
        saveMessage(conversationId, userId, "assistant", aiMessage, intent,
                convertor.parseResultToJson(parseResult), tokenCount, responseTime);

        // 维护父表：首次创建会话，后续更新汇总
        AiConversationPO existing = conversationMapper.selectOne(
                new LambdaQueryWrapper<AiConversationPO>()
                        .eq(AiConversationPO::getConversationId, conversationId)
                        .last("LIMIT 1"));
        if (existing == null) {
            createConversation(conversationId, userId, parseResult, userMessage);
        } else {
            updateConversationSummary(conversationId, userMessage, aiMessage);
        }
    }

    /**
     * 创建会话父表记录（取首条用户消息截断作为标题）
     */
    private void createConversation(String conversationId, Long userId, NlpParseResult parseResult,
                                    String userMessage) {
        AiConversationPO po = new AiConversationPO();
        po.setConversationId(conversationId);
        po.setUserId(userId);
        po.setSessionType(parseResult != null && parseResult.getIntent() != null
                ? parseResult.getIntent().getCode() : null);
        po.setTitle(StrUtil.sub(userMessage, 0, TITLE_MAX_LENGTH));
        po.setLastMessage(StrUtil.sub(userMessage, 0, 200));
        po.setMessageCount(2);
        po.setUpdateTime(LocalDateTime.now());
        conversationMapper.insert(po);
    }

    /**
     * 更新会话父表汇总信息（最后消息预览、消息数、更新时间）
     */
    private void updateConversationSummary(String conversationId, String userMessage, String aiMessage) {
        AiConversationPO po = conversationMapper.selectOne(
                new LambdaQueryWrapper<AiConversationPO>()
                        .eq(AiConversationPO::getConversationId, conversationId)
                        .last("LIMIT 1"));
        if (po == null) {
            return;
        }
        int newCount = (po.getMessageCount() == null ? 0 : po.getMessageCount()) + 2;
        LambdaUpdateWrapper<AiConversationPO> update = new LambdaUpdateWrapper<>();
        update.eq(AiConversationPO::getConversationId, conversationId)
                .set(AiConversationPO::getLastMessage, StrUtil.sub(aiMessage, 0, 200))
                .set(AiConversationPO::getMessageCount, newCount)
                .set(AiConversationPO::getUpdateTime, LocalDateTime.now());
        conversationMapper.update(null, update);
    }

    /**
     * 写入单条消息子表
     */
    private void saveMessage(String conversationId, Long userId, String role, String content,
                             String intent, String entities, int tokenCount, int responseTime) {
        AiConversationMessagePO po = new AiConversationMessagePO();
        po.setConversationId(conversationId);
        po.setUserId(userId);
        po.setRole(role);
        po.setContent(content);
        po.setIntent(intent);
        po.setEntities(entities);
        po.setTokenCount(tokenCount);
        po.setResponseTime(responseTime);
        po.setCreateTime(LocalDateTime.now());
        messageMapper.insert(po);
    }

    /**
     * 查询用户会话列表
     */
    @Override
    public List<ConversationRecord> listConversations(Long userId) {
        List<AiConversationPO> poList = conversationMapper.selectByUserId(userId);
        return poList.stream().map(convertor::toConversationRecord).toList();
    }

    /**
     * 查询会话详情（含消息明细），并校验归属用户
     */
    @Override
    public ConversationRecord getConversation(String conversationId, Long userId) {
        AiConversationPO po = conversationMapper.selectOne(
                new LambdaQueryWrapper<AiConversationPO>()
                        .eq(AiConversationPO::getConversationId, conversationId)
                        .eq(AiConversationPO::getDeleted, 0)
                        .last("LIMIT 1"));
        if (po == null) {
            return null;
        }
        // 越权校验：未登录(userId=null)不允许访问
        if (userId == null || !userId.equals(po.getUserId())) {
            return null;
        }
        ConversationRecord record = convertor.toConversationRecord(po);
        List<AiConversationMessagePO> msgPoList = messageMapper.selectByConversationId(conversationId);
        List<ConversationMessage> messages = convertor.toConversationMessageList(msgPoList);
        record.setMessages(messages);
        return record;
    }

    /**
     * 逻辑删除会话（父表 + 子表）
     */
    @Override
    public void deleteByConversationId(String conversationId, Long userId) {
        AiConversationPO po = conversationMapper.selectOne(
                new LambdaQueryWrapper<AiConversationPO>()
                        .eq(AiConversationPO::getConversationId, conversationId)
                        .last("LIMIT 1"));
        if (po == null || userId == null || !userId.equals(po.getUserId())) {
            log.warn("删除会话越权或会话不存在: conversationId={}, userId={}", conversationId, userId);
            return;
        }
        conversationMapper.logicDeleteByConversationId(conversationId);
        messageMapper.logicDeleteByConversationId(conversationId);
    }

    /**
     * 重命名会话（更新父表标题）
     */
    @Override
    public void rename(String conversationId, Long userId, String title) {
        AiConversationPO po = conversationMapper.selectOne(
                new LambdaQueryWrapper<AiConversationPO>()
                        .eq(AiConversationPO::getConversationId, conversationId)
                        .last("LIMIT 1"));
        if (po == null || userId == null || !userId.equals(po.getUserId())) {
            log.warn("重命名会话越权或会话不存在: conversationId={}, userId={}", conversationId, userId);
            return;
        }
        LambdaUpdateWrapper<AiConversationPO> update = new LambdaUpdateWrapper<>();
        update.eq(AiConversationPO::getConversationId, conversationId)
                .set(AiConversationPO::getTitle, title)
                .set(AiConversationPO::getUpdateTime, LocalDateTime.now());
        conversationMapper.update(null, update);
    }

    /**
     * 查询会话归属用户ID
     */
    @Override
    public Long selectUserId(String conversationId) {
        AiConversationPO po = conversationMapper.selectOne(
                new LambdaQueryWrapper<AiConversationPO>()
                        .eq(AiConversationPO::getConversationId, conversationId)
                        .last("LIMIT 1"));
        return po == null ? null : po.getUserId();
    }
}
