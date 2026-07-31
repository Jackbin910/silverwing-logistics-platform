package com.silverwing.biz.dict.domain.model.query;

import com.silverwing.common.domain.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典类型查询条件（含分页参数）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DictTypeQuery extends PageRequest {

    /** 字典名称（模糊匹配） */
    private String dictName;

    /** 字典类型（模糊匹配） */
    private String dictType;

    /** 状态（0-正常 1-停用） */
    private String status;
}
