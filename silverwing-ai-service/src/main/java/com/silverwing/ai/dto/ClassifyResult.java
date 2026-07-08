package com.silverwing.ai.dto;

import com.silverwing.biz.ai.domain.enums.TextCategoryEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文本分类结果 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassifyResult {

    /**
     * 分类结果
     */
    private TextCategoryEnum category;

    /**
     * 置信度（0-1）
     */
    private Double confidence;

    /**
     * 分类描述
     */
    private String description;
}
