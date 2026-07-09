package com.silverwing.admin.application.query;

import com.silverwing.admin.application.dto.UserResponse;
import com.silverwing.biz.iam.domain.model.query.UserQuery;
import com.silverwing.common.domain.PageResult;

import java.util.List;

/**
 * 用户查询服务（CQRS 读侧）
 * <p>返回经由 UserConvertor 映射的 {@link UserResponse}，避免直接暴露领域聚合根（含密码）。</p>
 */
public interface UserQueryService {

    PageResult<UserResponse> list(UserQuery query);

    UserResponse getById(Long id);

    List<Long> getUserRoleIds(Long userId);
}
