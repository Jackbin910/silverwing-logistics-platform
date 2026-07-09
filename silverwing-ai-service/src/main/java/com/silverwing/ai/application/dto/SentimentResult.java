package com.silverwing.ai.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 情感分析结果 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SentimentResult {

    /**
     * 情感标签：POSITIVE / NEUTRAL / NEGATIVE
     */
    private String label;

    /**
     * 情感得分（-1到1，-1为极度负面，1为极度正面）
     */
    private Double score;

    /**
     * 情感描述
     */
    private String description;
}
