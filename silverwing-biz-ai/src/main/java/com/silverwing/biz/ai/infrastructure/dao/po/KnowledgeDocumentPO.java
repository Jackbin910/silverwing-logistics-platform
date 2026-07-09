package com.silverwing.biz.ai.infrastructure.dao.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识库文档持久化对象（PO）
 * <p>
 * 与数据库表 ai_knowledge_document 一一对应，仅承载数据，不包含领域行为。
 * 通过 KnowledgeDocumentInfraConvertor 与领域实体（KnowledgeDocumentAggregate）互转。
 * </p>
 */
@Data
@TableName(value = "ai_knowledge_document")
public class KnowledgeDocumentPO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String documentId;
    private String title;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private Integer wordCount;
    private Integer chunkCount;
    private Integer status;
    private String errorMsg;

    private String createBy;
    private String updateBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
