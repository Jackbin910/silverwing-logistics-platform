package com.silverwing.biz.iam.domain.model.query;

import com.silverwing.common.domain.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 权限分页查询条件
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PermissionQuery extends PageRequest {

    /** 关键词（模糊匹配权限编码或权限名称） */
    private String keyword;

    /** 状态（可选：1启用 / 0禁用） */
    private Integer status;
}
