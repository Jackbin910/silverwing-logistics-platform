package com.silverwing.ai.application.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 知识库文档响应DTO
 * <p>由 KnowledgeDocumentAggregate 经 AiConvertor 映射得到，作为对外展示的文档视图，不暴露领域聚合根。</p>
 */
@Data
public class KnowledgeDocumentDTO implements Serializable {

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
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
