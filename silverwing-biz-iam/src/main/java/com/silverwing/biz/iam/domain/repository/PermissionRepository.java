package com.silverwing.biz.iam.domain.repository;

import com.silverwing.biz.iam.domain.model.SysPermission;

import java.util.List;

/**
 * 权限仓储接口（领域契约）
 */
public interface PermissionRepository {

    SysPermission findById(Long id);

    List<SysPermission> findAll();

    void save(SysPermission permission);

    void deleteById(Long id);

    /** 查询用户拥有的权限标识列表（用于登录鉴权） */
    List<String> findPermissionCodesByUserId(Long userId);
}
