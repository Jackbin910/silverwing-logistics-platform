package com.silverwing.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 业务查询结果 DTO
 * 用于意图处理器返回结构化业务数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BizQueryResult {

    /**
     * 结果标题
     */
    private String title;

    /**
     * 结构化业务数据
     */
    private Map<String, Object> data;

    /**
     * 提示信息（可选）
     */
    private String message;
}
