package com.silverwing.biz.dept.domain.model.query;

import lombok.Data;

/**
 * 部门查询条件（领域层）。
 */
@Data
public class DeptQuery {

    /** 部门名称（模糊匹配） */
    private String deptName;

    /** 部门状态（0-正常 1-停用） */
    private String status;

    /** 排除指定部门ID及其子部门（编辑回显时过滤） */
    private Long excludeDeptId;
}
