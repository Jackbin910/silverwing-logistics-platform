package com.silverwing.admin.application.query;

import com.silverwing.biz.iam.domain.model.aggregate.SysRoleAggregate;
import com.silverwing.biz.iam.domain.model.query.RoleQuery;
import com.silverwing.common.domain.PageResult;

import java.util.List;

/**
 * 角色查询服务（CQRS 读侧）
 */
public interface RoleQueryService {

    PageResult<SysRoleAggregate> list(RoleQuery query);

    List<SysRoleAggregate> listAllEnabled();

    SysRoleAggregate getById(Long id);

    List<Long> getRolePermissionIds(Long roleId);
}
