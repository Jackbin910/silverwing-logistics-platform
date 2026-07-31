package com.silverwing.admin.client;

import com.silverwing.admin.application.command.SaveDeptCommand;
import com.silverwing.admin.application.dto.DeptResponse;
import com.silverwing.admin.application.dto.DeptRoleTreeResponse;
import com.silverwing.admin.application.dto.DeptTreeResponse;
import com.silverwing.admin.application.query.DeptQuery;

import java.util.List;

/**
 * 部门上下文防腐层端口，供应用层调用。
 */
public interface DeptClient {

    /** 创建部门 */
    DeptResponse create(SaveDeptCommand command);

    /** 更新部门 */
    void update(Long deptId, SaveDeptCommand command);

    /** 删除部门 */
    void delete(Long deptId);

    /** 保存部门排序 */
    void saveSort(Long deptId, Integer orderNum);

    /** 根据主键查询部门 */
    DeptResponse getById(Long deptId);

    /** 条件查询部门列表 */
    List<DeptResponse> list(DeptQuery query);

    /** 查询全部部门（排除指定部门及其子部门） */
    List<DeptResponse> listExcludeChild(Long deptId);

    /** 部门树（下拉树） */
    List<DeptTreeResponse> treeSelect();

    /** 角色关联部门树 */
    DeptRoleTreeResponse roleDeptTreeSelect(Long roleId);
}
