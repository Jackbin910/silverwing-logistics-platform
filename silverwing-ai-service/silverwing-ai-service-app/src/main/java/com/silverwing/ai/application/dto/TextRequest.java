package com.silverwing.ai.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 通用文本请求 DTO
 */
@Data
public class TextRequest {

    /**
     * 待处理的文本内容
     */
    @NotBlank(message = "{validation.text.text.notblank}")
    private String text;
}
