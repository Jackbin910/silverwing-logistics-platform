package com.silverwing.ai.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * AI会话领域模型（父表）
 * <p>
 * 聚合根，承载会话级信息；会话下的消息明细以 {@link ConversationMessage} 列表形式挂在会话下。
 * </p>
 *
 * @author silverwing
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationRecord {

    /**
     * 会话ID
     */
    private String conversationId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 会话类型
     */
    private String sessionType;

    /**
     * 会话标题
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
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 消息明细（子表）
     */
    @Builder.Default
    private List<ConversationMessage> messages = new ArrayList<>();
}
