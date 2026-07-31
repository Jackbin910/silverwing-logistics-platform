package com.silverwing.biz.iam.domain.service;

import com.silverwing.biz.iam.domain.model.aggregate.SysPermissionAggregate;

import java.util.List;

/**
 * 权限领域服务
 * <p>封装权限持久化相关的领域编排。</p>
 */
public interface IPermissionDomainService {

    /**
     * 保存权限（新增或更新）
     */
    SysPermissionAggregate save(SysPermissionAggregate permission);

    /**
     * 删除权限
     */
    void deleteById(Long id);

    /**
     * 校验同级下权限名称是否唯一（不存在冲突时正常返回）
     *
     * @param id           权限ID（新增传 null）
     * @param parentId     父级ID
     * @param permissionName 权限名称
     */
    void checkPermissionNameUnique(Long id, Long parentId, String permissionName);

    /**
     * 校验路由名称（route_name）是否唯一（不存在冲突时正常返回）
     *
     * @param id        权限ID（新增传 null）
     * @param routeName 路由名称
     */
    void checkRouteConfigUnique(Long id, String routeName);

    /**
     * 保存权限（菜单）排序，按传入ID顺序递增排序值
     *
     * @param ids 按展示顺序排列的权限ID列表
     */
    void updateSort(List<Long> ids);
}
