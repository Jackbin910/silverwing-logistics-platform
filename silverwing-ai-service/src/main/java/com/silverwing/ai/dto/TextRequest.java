package com.silverwing.ai.dto;

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
    @NotBlank(message = "文本内容不能为空")
    private String text;
}
