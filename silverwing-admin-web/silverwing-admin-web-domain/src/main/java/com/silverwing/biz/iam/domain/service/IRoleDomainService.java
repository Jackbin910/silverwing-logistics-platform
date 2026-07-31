package com.silverwing.biz.iam.domain.service;

import com.silverwing.biz.iam.domain.model.aggregate.SysRoleAggregate;

import java.util.List;

/**
 * 角色领域服务
 * <p>封装角色注册（编码唯一性校验）与权限分配等编排逻辑。</p>
 */
public interface IRoleDomainService {

    /**
     * 注册角色（含角色编码唯一性校验）
     */
    SysRoleAggregate registerRole(SysRoleAggregate role);

    /**
     * 为角色分配权限（全量覆盖）
     */
    void assignPermissions(Long roleId, List<Long> permissionIds);

    /**
     * 更新角色信息（持久化）
     */
    SysRoleAggregate update(SysRoleAggregate role);

    /**
     * 删除角色（含级联清理）
     */
    void deleteById(Long id);

    /**
     * 切换角色状态
     */
    void changeStatus(Long roleId, Integer status);

    /**
     * 更新角色数据范围（含角色-部门关联）
     */
    void updateDataScope(Long roleId, Integer dataScope, List<Long> deptIds);

    /**
     * 批量删除角色（含级联清理）
     */
    void deleteByIds(List<Long> ids);
}
