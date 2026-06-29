package com.silverwing.auth.service;

import com.silverwing.auth.entity.SysRole;

import java.util.List;

/**
 * 角色服务接口
 */
public interface SysRoleService {

    /**
     * 根据用户ID查询角色列表
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    List<SysRole> getRolesByUserId(Long userId);

    /**
     * 根据角色编码查询角色
     *
     * @param roleCode 角色编码
     * @return 角色实体，不存在则返回 null
     */
    SysRole getByRoleCode(String roleCode);

    /**
     * 为用户分配角色
     *
     * @param userId 用户ID
     * @param roleId 角色ID
     */
    void assignRoleToUser(Long userId, Long roleId);

    /**
     * 移除用户的角色
     *
     * @param userId 用户ID
     * @param roleId 角色ID
     */
    void removeRoleFromUser(Long userId, Long roleId);
}
