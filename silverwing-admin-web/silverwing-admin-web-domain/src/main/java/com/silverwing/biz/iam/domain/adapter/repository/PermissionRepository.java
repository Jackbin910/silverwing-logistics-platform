package com.silverwing.biz.iam.domain.adapter.repository;

import com.silverwing.biz.iam.domain.model.aggregate.SysPermissionAggregate;
import com.silverwing.biz.iam.domain.model.query.PermissionQuery;
import com.silverwing.common.domain.PageResult;

import java.util.List;

/**
 * 权限仓储接口（领域契约/端口）
 */
public interface PermissionRepository {

    SysPermissionAggregate findById(Long id);

    List<SysPermissionAggregate> findAll();

    /**
     * 分页查询权限列表
     *
     * @param query 分页查询条件（含关键词、状态筛选）
     * @return 分页后的权限聚合根
     */
    PageResult<SysPermissionAggregate> findPage(PermissionQuery query);

    void save(SysPermissionAggregate permission);

    void deleteById(Long id);

    /** 查询用户拥有的权限标识列表（用于登录鉴权） */
    List<String> findPermissionCodesByUserId(Long userId);
}
