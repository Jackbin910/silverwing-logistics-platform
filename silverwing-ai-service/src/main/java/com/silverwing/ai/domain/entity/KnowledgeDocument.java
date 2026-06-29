package com.silverwing.ai.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识库文档实体
 * 存储已导入知识库的元信息（文档ID、标题、分类等）
 */
@Data
@TableName("ai_knowledge_document")
public class KnowledgeDocument {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 文档唯一标识(UUID)
     */
    private String documentId;

    /**
     * 文档标题
     */
    private String title;

    /**
     * 文档分类（如：设备手册、FAQ、维护记录）
     */
    private String category;

    /**
     * 来源类型（manual、web、faq）
     */
    private String sourceType;

    /**
     * 仓库ID（用于知识隔离）
     */
    private String warehouseId;

    /**
     * 设备类型
     */
    private String deviceType;

    /**
     * 文档字数
     */
    private Integer wordCount;

    /**
     * 导入的分片数量
     */
    private Integer chunkCount;

    /**
     * 文档状态（0-待处理、1-已导入、2-导入失败）
     */
    private Integer status;

    /**
     * 错误信息
     */
    private String errorMsg;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 是否删除（0-否、1-是）
     */
    @TableLogic
    private Integer deleted;
}
