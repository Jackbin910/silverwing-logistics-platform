package com.silverwing.admin.application.query;

import com.silverwing.biz.iam.domain.model.aggregate.SysPermissionAggregate;

import java.util.List;

/**
 * 权限查询服务（CQRS 读侧）
 */
public interface PermissionQueryService {

    List<SysPermissionAggregate> listAll();

    SysPermissionAggregate getById(Long id);
}
