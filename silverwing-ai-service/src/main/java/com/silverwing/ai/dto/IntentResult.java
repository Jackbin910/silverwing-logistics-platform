package com.silverwing.ai.dto;

import com.silverwing.biz.ai.domain.enums.IntentEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 意图识别结果 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntentResult {

    /**
     * 识别出的意图
     */
    private IntentEnum intent;

    /**
     * 置信度（0-1）
     */
    private Double confidence;

    /**
     * 意图描述
     */
    private String description;
}
