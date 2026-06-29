package com.silverwing.auth.service;

import com.silverwing.auth.entity.SysPermission;

import java.util.List;

/**
 * 权限服务接口
 */
public interface SysPermissionService {

    /**
     * 根据用户ID查询其拥有的权限标识列表
     *
     * @param userId 用户ID
     * @return 权限标识列表，如 ["system:user:list"]
     */
    List<String> getPermissionCodesByUserId(Long userId);

    /**
     * 查询全部权限列表
     *
     * @return 权限列表
     */
    List<SysPermission> listAll();

    /**
     * 根据角色ID查询已分配的权限ID列表
     *
     * @param roleId 角色ID
     * @return 权限ID列表
     */
    List<Long> getPermissionIdsByRoleId(Long roleId);

    /**
     * 为角色分配权限（先清空后批量插入）
     *
     * @param roleId        角色ID
     * @param permissionIds 权限ID列表
     */
    void assignPermissionsToRole(Long roleId, List<Long> permissionIds);

    /**
     * 刷新指定在线用户的权限缓存（Session）
     *
     * @param userId 用户ID
     */
    void refreshUserPermissionCache(Long userId);

}
