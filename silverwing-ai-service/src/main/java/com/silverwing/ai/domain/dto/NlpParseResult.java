package com.silverwing.ai.domain.dto;

import com.silverwing.ai.domain.enums.IntentEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * NLP 综合解析结果 DTO
 * 用于一次调用同时完成意图识别和实体提取
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NlpParseResult {

    /**
     * 识别出的意图
     */
    private IntentEnum intent;

    /**
     * 提取出的实体列表
     */
    private List<EntityResult> entities;

    /**
     * 置信度
     */
    private Double confidence;
}
