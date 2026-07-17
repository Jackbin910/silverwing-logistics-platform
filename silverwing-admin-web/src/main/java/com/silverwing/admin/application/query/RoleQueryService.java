package com.silverwing.admin.application.query;

import com.silverwing.admin.application.dto.RoleResponse;
import com.silverwing.admin.application.query.RolePageQuery;
import com.silverwing.common.domain.PageResult;

import java.util.List;

/**
 * 角色查询服务（CQRS 读侧）
 * <p>通过 {@link IamRoleClient} 防腐层端口访问 biz-iam，返回本模块 {@link RoleResponse}。</p>
 */
public interface RoleQueryService {

    PageResult<RoleResponse> list(RolePageQuery query);

    List<RoleResponse> listAllEnabled();

    RoleResponse getById(Long id);

    List<Long> getRolePermissionIds(Long roleId);
}
