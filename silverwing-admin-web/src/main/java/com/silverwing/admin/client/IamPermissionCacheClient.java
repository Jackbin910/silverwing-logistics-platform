package com.silverwing.admin.client;

/**
 * 权限缓存防腐层端口
 * <p>封装 Sa-Token Session 权限/角色缓存的刷新操作，避免应用层直接耦合 Sa-Token 与
 * biz-iam 的仓储实现。具体实现位于 {@code client.impl} 包。</p>
 */
public interface IamPermissionCacheClient {

    /**
     * 刷新指定用户的权限缓存
     */
    void refreshUserPermissionCache(Long userId);

    /**
     * 批量刷新角色下所有在线用户的权限缓存
     */
    void refreshRoleUserCache(Long roleId);
}
