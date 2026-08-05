package com.silverwing.biz.ai.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.silverwing.biz.ai.infrastructure.dao.po.AiConversationPO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * AI会话主表 Mapper（父表）
 *
 * @author silverwing
 */
public interface AiConversationMapper extends BaseMapper<AiConversationPO> {

    /**
     * 查询指定用户的会话列表（按最后更新时间倒序）
     *
     * @param userId 用户ID
     * @return 会话列表
     */
    List<AiConversationPO> selectByUserId(@Param("userId") Long userId);

    /**
     * 逻辑删除指定会话（父表标记 deleted=1）
     *
     * @param conversationId 会话ID
     * @return 影响行数
     */
    int logicDeleteByConversationId(@Param("conversationId") String conversationId);
}
