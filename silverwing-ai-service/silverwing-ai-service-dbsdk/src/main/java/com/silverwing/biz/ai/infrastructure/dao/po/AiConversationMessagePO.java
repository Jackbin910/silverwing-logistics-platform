package com.silverwing.biz.ai.infrastructure.dao.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI会话消息表持久化对象（子表）
 * <p>
 * 对应表 {@code ai_conversation_message}，一行代表一条消息（user 或 assistant）。
 * 同一会话的多条消息通过 {@code conversation_id} 关联父表 {@code ai_conversation}。
 * </p>
 *
 * @author silverwing
 */
@Data
@TableName(value = "ai_conversation_message")
public class AiConversationMessagePO {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 对话会话ID（与父表一致）
     */
    private String conversationId;

    /**
     * 用户ID（冗余存储，便于越权校验，无需回查父表）
     */
    private Long userId;

    /**
     * 消息角色：user / assistant
     */
    private String role;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 识别的意图
     */
    private String intent;

    /**
     * 提取的实体（JSON格式）
     */
    private String entities;

    /**
     * 使用的token数量
     */
    private Integer tokenCount;

    /**
     * 响应时间（毫秒）
     */
    private Integer responseTime;

    /**
     * 逻辑删除：0未删 1已删
     */
    private Integer deleted;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
