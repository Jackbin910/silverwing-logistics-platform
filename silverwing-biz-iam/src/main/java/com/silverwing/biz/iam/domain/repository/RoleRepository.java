package com.silverwing.biz.iam.domain.repository;

import com.silverwing.common.domain.PageResult;
import com.silverwing.biz.iam.domain.model.RoleQuery;
import com.silverwing.biz.iam.domain.model.SysRole;

import java.util.List;

/**
 * 角色仓储接口（领域契约）
 */
public interface RoleRepository {

    SysRole findById(Long id);

    SysRole findByRoleCode(String roleCode);

    boolean existsByRoleCode(String roleCode);

    void save(SysRole role);

    void deleteById(Long id);

    PageResult<SysRole> findPage(RoleQuery query);

    List<SysRole> findAllEnabled();

    /** 查询用户拥有的角色列表（用于登录鉴权） */
    List<SysRole> findRolesByUserId(Long userId);

    /** 查询角色下关联的用户ID列表 */
    List<Long> findUserIdsByRoleId(Long roleId);

    /** 为角色分配权限（全量覆盖） */
    void assignPermissions(Long roleId, List<Long> permissionIds);

    /** 查询角色已分配的权限ID列表 */
    List<Long> findPermissionIdsByRoleId(Long roleId);
}
