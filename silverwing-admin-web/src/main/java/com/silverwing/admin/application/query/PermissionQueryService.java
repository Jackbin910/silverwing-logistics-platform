package com.silverwing.admin.application.query;

import com.silverwing.admin.application.dto.PermissionResponse;

import java.util.List;

/**
 * 权限查询服务（CQRS 读侧）
 * <p>返回经由 PermissionConvertor 映射的 {@link PermissionResponse}，避免直接暴露领域聚合根。</p>
 */
public interface PermissionQueryService {

    List<PermissionResponse> listAll();

    PermissionResponse getById(Long id);
}
