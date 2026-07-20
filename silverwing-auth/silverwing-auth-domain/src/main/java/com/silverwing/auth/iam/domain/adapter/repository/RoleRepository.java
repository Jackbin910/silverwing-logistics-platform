package com.silverwing.auth.iam.domain.adapter.repository;

import com.silverwing.auth.iam.domain.model.aggregate.AuthRoleAggregate;

import java.util.List;

/**
 * 角色仓储端口（领域契约）
 */
public interface RoleRepository {

    /**
     * 查询用户拥有的角色列表（用于登录鉴权写入 Session）
     *
     * @param userId 用户ID
     * @return 角色聚合根列表
     */
    List<AuthRoleAggregate> findRolesByUserId(Long userId);
}
