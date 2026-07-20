package com.silverwing.auth.iam.domain.adapter.repository;

import java.util.List;

/**
 * 权限仓储端口（领域契约）
 */
public interface PermissionRepository {

    /**
     * 查询用户拥有的权限标识列表（用于登录鉴权写入 Session）
     *
     * @param userId 用户ID
     * @return 权限标识列表（如 system:user:list）
     */
    List<String> findPermissionCodesByUserId(Long userId);
}
