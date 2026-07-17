package com.silverwing.ai.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * NLP 综合分析结果 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NlpAnalyzeResult {

    /**
     * 意图识别结果
     */
    private IntentResult intentResult;

    /**
     * 实体提取结果
     */
    private List<EntityResult> entities;

    /**
     * 文本分类结果
     */
    private ClassifyResult classifyResult;

    /**
     * 情感分析结果
     */
    private SentimentResult sentimentResult;
}
