package com.silverwing.biz.ai.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.silverwing.biz.ai.infrastructure.dao.po.AiConversationMessagePO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * AI会话消息表 Mapper（子表）
 *
 * @author silverwing
 */
public interface AiConversationMessageMapper extends BaseMapper<AiConversationMessagePO> {

    /**
     * 查询指定会话下的全部消息明细（按创建时间正序）
     *
     * @param conversationId 会话ID
     * @return 消息明细列表
     */
    List<AiConversationMessagePO> selectByConversationId(@Param("conversationId") String conversationId);

    /**
     * 查询指定会话的首条消息（用于会话标题与归属校验）
     *
     * @param conversationId 会话ID
     * @return 首条消息
     */
    AiConversationMessagePO selectFirstByConversationId(@Param("conversationId") String conversationId);

    /**
     * 逻辑删除指定会话下的全部消息（子表标记 deleted=1）
     *
     * @param conversationId 会话ID
     * @return 影响行数
     */
    int logicDeleteByConversationId(@Param("conversationId") String conversationId);
}
