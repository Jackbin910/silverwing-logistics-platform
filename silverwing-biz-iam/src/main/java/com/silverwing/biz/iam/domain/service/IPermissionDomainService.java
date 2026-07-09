package com.silverwing.biz.iam.domain.service;

import com.silverwing.biz.iam.domain.model.aggregate.SysPermissionAggregate;

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
}
