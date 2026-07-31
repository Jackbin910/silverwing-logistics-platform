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

    /**
     * 按条件查询权限列表（非分页，用于树形构建）
     */
    List<SysPermissionAggregate> findList(PermissionQuery query);

    /**
     * 根据父级ID与权限名称查询同级权限（校验名称唯一）
     */
    SysPermissionAggregate findByNameAndParent(Long parentId, String permissionName);

    /**
     * 根据路由名称查询权限（校验路由唯一）
     */
    SysPermissionAggregate findByRouteName(String routeName);

    /**
     * 是否存在子级权限
     */
    boolean hasChildByParentId(Long parentId);

    /**
     * 统计关联某权限的角色数量
     */
    long countByRoleId(Long permissionId);

    /**
     * 查询角色已分配的权限ID列表
     */
    List<Long> findIdsByRoleId(Long roleId);

    /**
     * 按传入顺序保存权限排序
     */
    void updateSort(List<Long> ids);
}
