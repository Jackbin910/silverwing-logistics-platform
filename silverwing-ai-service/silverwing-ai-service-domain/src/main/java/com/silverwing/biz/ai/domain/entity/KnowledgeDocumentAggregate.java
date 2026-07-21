package com.silverwing.biz.ai.domain.entity;

import com.silverwing.common.entity.DomainEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 知识库文档聚合根
 * <p>
 * 存储已导入知识库的元信息（文档ID、标题、文件信息等）；持久化映射由基础设施层
 * KnowledgeDocumentPO（@TableName）承担，聚合根本身不持有表注解。
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class KnowledgeDocumentAggregate extends DomainEntity {

    private Long id;

    /** 文档唯一标识(UUID) */
    private String documentId;

    /** 文档标题 */
    private String title;

    /** 原始文件名 */
    private String fileName;

    /** 文件类型（如：pdf、docx、md） */
    private String fileType;

    /** 文件大小（字节） */
    private Long fileSize;

    /** 原始文件在对象存储（RustFS）中的 Key */
    private String fileKey;

    /** 原始文件在对象存储中的访问 URL */
    private String fileUrl;

    /** 文档字数 */
    private Integer wordCount;

    /** 导入的分片数量 */
    private Integer chunkCount;

    /** 文档状态（0-待处理、1-已导入、2-导入失败） */
    private Integer status;

    /** 错误信息 */
    private String errorMsg;
}
