package com.silverwing.admin.application.query;

import com.silverwing.admin.application.dto.DeptResponse;
import com.silverwing.admin.application.dto.DeptRoleTreeResponse;
import com.silverwing.admin.application.dto.DeptTreeResponse;

import java.util.List;

/**
 * 部门查询服务。
 */
public interface DeptQueryService {

    /** 根据主键查询 */
    DeptResponse getById(Long deptId);

    /** 条件查询列表 */
    List<DeptResponse> list(DeptQuery query);

    /** 查询排除指定节点及其子节点的列表 */
    List<DeptResponse> listExcludeChild(Long deptId);

    /** 部门下拉树 */
    List<DeptTreeResponse> treeSelect();

    /** 角色关联部门树 */
    DeptRoleTreeResponse roleDeptTreeSelect(Long roleId);
}
