package com.silverwing.biz.ai.infrastructure.dao.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI会话主表持久化对象（父表）
 * <p>
 * 对应表 {@code ai_conversation}，一行代表一个会话，承载会话级字段。
 * 会话下的每轮问答明细存储在子表 {@code ai_conversation_message}。
 * createTime / updateTime 由 MyBatis-Plus 自动填充，deleted 为逻辑删除标记。
 * </p>
 *
 * @author silverwing
 */
@Data
@TableName(value = "ai_conversation")
public class AiConversationPO {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 对话会话ID（与前端 sessionId 一致）
     */
    private String conversationId;

    /**
     * 用户ID（Sa-Token loginId，未登录为 null）
     */
    private Long userId;

    /**
     * 会话类型（order_query、device_query、knowledge_qa）
     */
    private String sessionType;

    /**
     * 会话标题（取首条用户消息截断，支持重命名）
     */
    private String title;

    /**
     * 最后一条消息预览
     */
    private String lastMessage;

    /**
     * 消息条数
     */
    private Integer messageCount;

    /**
     * 更新时间（自动填充）
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除：0未删 1已删
     */
    private Integer deleted;

    /**
     * 创建时间（自动填充）
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

}
