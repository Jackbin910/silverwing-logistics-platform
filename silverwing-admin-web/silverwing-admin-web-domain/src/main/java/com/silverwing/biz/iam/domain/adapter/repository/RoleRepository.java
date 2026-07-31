package com.silverwing.biz.iam.domain.adapter.repository;

import com.silverwing.common.domain.PageResult;
import com.silverwing.biz.iam.domain.model.aggregate.SysRoleAggregate;
import com.silverwing.biz.iam.domain.model.query.RoleQuery;

import java.util.List;

/**
 * 角色仓储接口（领域契约/端口）
 */
public interface RoleRepository {

    SysRoleAggregate findById(Long id);

    SysRoleAggregate findByRoleCode(String roleCode);

    boolean existsByRoleCode(String roleCode);

    void save(SysRoleAggregate role);

    void deleteById(Long id);

    PageResult<SysRoleAggregate> findPage(RoleQuery query);

    List<SysRoleAggregate> findAllEnabled();

    /** 查询用户拥有的角色列表（用于登录鉴权） */
    List<SysRoleAggregate> findRolesByUserId(Long userId);

    /** 查询角色下关联的用户ID列表 */
    List<Long> findUserIdsByRoleId(Long roleId);

    /** 为角色分配权限（全量覆盖） */
    void assignPermissions(Long roleId, List<Long> permissionIds);

    /** 查询角色已分配的权限ID列表 */
    List<Long> findPermissionIdsByRoleId(Long roleId);

    /** 切换角色状态 */
    void updateStatus(Long roleId, Integer status);

    /** 更新角色数据范围（含角色-部门关联） */
    void updateDataScope(Long roleId, Integer dataScope, List<Long> deptIds);

    /** 查询全部角色（导出用，不缓存） */
    List<SysRoleAggregate> findAll();

    /** 批量删除角色（含级联清理） */
    void deleteByIds(List<Long> ids);
}
