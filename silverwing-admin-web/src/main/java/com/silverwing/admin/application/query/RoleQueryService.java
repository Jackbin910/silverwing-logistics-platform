package com.silverwing.admin.application.query;

import com.silverwing.admin.application.dto.RoleResponse;
import com.silverwing.biz.iam.domain.model.query.RoleQuery;
import com.silverwing.common.domain.PageResult;

import java.util.List;

/**
 * 角色查询服务（CQRS 读侧）
 * <p>返回经由 RoleConvertor 映射的 {@link RoleResponse}，避免直接暴露领域聚合根。</p>
 */
public interface RoleQueryService {

    PageResult<RoleResponse> list(RoleQuery query);

    List<RoleResponse> listAllEnabled();

    RoleResponse getById(Long id);

    List<Long> getRolePermissionIds(Long roleId);
}
