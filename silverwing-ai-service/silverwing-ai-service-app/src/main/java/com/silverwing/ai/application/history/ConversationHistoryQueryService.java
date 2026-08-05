package com.silverwing.ai.application.history;

import cn.dev33.satoken.stp.StpUtil;
import com.silverwing.ai.application.dto.ConversationMessageResponse;
import com.silverwing.ai.application.dto.ConversationMetaResponse;
import com.silverwing.ai.domain.model.ConversationMessage;
import com.silverwing.ai.domain.model.ConversationRecord;
import com.silverwing.ai.domain.repository.ConversationHistoryRepository;
import com.silverwing.common.domain.ResultCode;
import com.silverwing.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 对话历史查询服务
 * <p>提供当前登录用户的会话列表与消息明细查询，并做会话归属越权校验。</p>
 *
 * @author silverwing
 */
@Slf4j
@Service
public class ConversationHistoryQueryService {

    private final ConversationHistoryRepository historyRepository;

    public ConversationHistoryQueryService(ConversationHistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    /**
     * 查询当前用户的会话列表（按最后更新时间倒序，父表维度）
     *
     * @return 会话列表
     */
    public List<ConversationMetaResponse> listConversations() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<ConversationRecord> records = historyRepository.listConversations(userId);
        return records.stream().map(this::toMeta).collect(Collectors.toList());
    }

    /**
     * 查询指定会话的消息明细（按时间正序），并校验会话归属当前用户
     *
     * @param conversationId 会话ID
     * @return 消息明细列表
     */
    public List<ConversationMessageResponse> getMessages(String conversationId) {
        Long userId = StpUtil.getLoginIdAsLong();
        ConversationRecord record = historyRepository.getConversation(conversationId, userId);
        if (record == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "ai.conversation.not.found");
        }
        return record.getMessages().stream().map(this::toMessage).collect(Collectors.toList());
    }

    private ConversationMetaResponse toMeta(ConversationRecord r) {
        ConversationMetaResponse resp = new ConversationMetaResponse();
        resp.setConversationId(r.getConversationId());
        resp.setTitle(r.getTitle());
        resp.setLastMessage(r.getLastMessage());
        resp.setSessionType(r.getSessionType());
        resp.setMessageCount(r.getMessageCount());
        resp.setUpdateTime(r.getUpdateTime());
        resp.setCreateTime(r.getCreateTime());
        return resp;
    }

    private ConversationMessageResponse toMessage(ConversationMessage m) {
        ConversationMessageResponse resp = new ConversationMessageResponse();
        resp.setId(m.getId());
        resp.setConversationId(m.getConversationId());
        resp.setRole(m.getRole());
        resp.setContent(m.getContent());
        resp.setIntent(m.getIntent());
        resp.setCreateTime(m.getCreateTime());
        return resp;
    }
}
