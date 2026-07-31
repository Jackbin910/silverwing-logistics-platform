package com.silverwing.biz.dict.domain.model.query;

import com.silverwing.common.domain.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典数据查询条件（含分页参数）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DictDataQuery extends PageRequest {

    /** 字典标签（模糊匹配） */
    private String dictLabel;

    /** 字典键值（模糊匹配） */
    private String dictValue;

    /** 字典类型 */
    private String dictType;

    /** 状态（0-正常 1-停用） */
    private String status;
}
