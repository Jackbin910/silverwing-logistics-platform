package com.silverwing.ai.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 知识库导入请求 DTO
 * 用于接收需要向量化并存入知识库的文档内容
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeIngestRequest {

    /**
     * 文档标题
     */
    @NotBlank(message = "文档标题不能为空")
    private String title;

    /**
     * 文档内容（纯文本）
     */
    @NotBlank(message = "文档内容不能为空")
    private String content;

    /**
     * 文档分类（如：设备手册、FAQ、维护记录、操作规程）
     */
    private String category;

    /**
     * 来源类型（如：manual、web、faq）
     */
    private String sourceType;

    /**
     * 仓库 ID（可选，用于按仓库隔离知识）
     */
    private String warehouseId;

    /**
     * 设备类型（可选，用于按设备类型过滤）
     */
    private String deviceType;
}
