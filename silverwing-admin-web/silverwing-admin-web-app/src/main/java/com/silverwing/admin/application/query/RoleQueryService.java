package com.silverwing.admin.application.query;

import com.silverwing.admin.application.dto.DeptRoleTreeResponse;
import com.silverwing.admin.application.dto.RoleResponse;
import com.silverwing.admin.application.dto.UserResponse;
import com.silverwing.admin.application.query.RolePageQuery;
import com.silverwing.common.domain.PageResult;

import java.util.List;

/**
 * 角色查询服务（CQRS 读侧）
 */
public interface RoleQueryService {

    PageResult<RoleResponse> list(RolePageQuery query);

    List<RoleResponse> listAllEnabled();

    RoleResponse getById(Long id);

    List<Long> getRolePermissionIds(Long roleId);

    /**
     * 查询全部角色（用于导出）
     */
    List<RoleResponse> listAll();

    /**
     * 查询角色对应的部门树（勾选节点 + 部门树）
     */
    DeptRoleTreeResponse roleDeptTree(Long roleId);

    /**
     * 查询角色已分配的用户列表（分页）
     */
    PageResult<UserResponse> allocatedList(Long roleId, UserPageQuery query);

    /**
     * 查询角色未分配的用户列表（分页）
     */
    PageResult<UserResponse> unallocatedList(Long roleId, UserPageQuery query);
}
