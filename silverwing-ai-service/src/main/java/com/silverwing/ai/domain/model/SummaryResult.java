package com.silverwing.ai.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文本摘要结果 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SummaryResult {

    /**
     * 摘要内容
     */
    private String summary;

    /**
     * 原文长度
     */
    private Integer originalLength;

    /**
     * 摘要长度
     */
    private Integer summaryLength;
}
