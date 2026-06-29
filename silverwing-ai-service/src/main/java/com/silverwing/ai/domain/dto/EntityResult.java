package com.silverwing.ai.domain.dto;

import com.silverwing.ai.domain.enums.EntityTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 命名实体提取结果 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntityResult {

    /**
     * 实体类型
     */
    private EntityTypeEnum type;

    /**
     * 实体值
     */
    private String value;

    /**
     * 实体在原文中的起始位置
     */
    private Integer start;

    /**
     * 实体在原文中的结束位置
     */
    private Integer end;
}
