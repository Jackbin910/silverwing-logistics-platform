package com.silverwing.biz.iam.domain.adapter.repository;

import com.silverwing.biz.iam.domain.model.aggregate.SysPermissionAggregate;

import java.util.List;

/**
 * 权限仓储接口（领域契约/端口）
 */
public interface PermissionRepository {

    SysPermissionAggregate findById(Long id);

    List<SysPermissionAggregate> findAll();

    void save(SysPermissionAggregate permission);

    void deleteById(Long id);

    /** 查询用户拥有的权限标识列表（用于登录鉴权） */
    List<String> findPermissionCodesByUserId(Long userId);
}
