package com.silverwing.ai.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 知识库文档导入结果 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeIngestResult {

    /**
     * 文档唯一标识
     */
    private String documentId;

    /**
     * 文档标题
     */
    private String title;

    /**
     * 分片总数
     */
    private int chunkCount;

    /**
     * 文档字数
     */
    private Integer wordCount;

    /**
     * 导入状态
     */
    private String status;

    /**
     * 提示信息
     */
    private String message;

    /**
     * 原始文件在对象存储（RustFS）中的 Key
     */
    private String fileKey;

    /**
     * 原始文件在对象存储中的访问 URL
     */
    private String fileUrl;
}
