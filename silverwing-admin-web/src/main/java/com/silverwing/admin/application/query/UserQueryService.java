package com.silverwing.admin.application.query;

import com.silverwing.admin.application.dto.UserResponse;
import com.silverwing.admin.application.query.UserPageQuery;
import com.silverwing.common.domain.PageResult;

import java.util.List;

/**
 * 用户查询服务（CQRS 读侧）
 * <p>通过 {@link IamUserClient} 防腐层端口访问 biz-iam，返回本模块 {@link UserResponse}，
 * 避免直接暴露领域聚合根（含密码）。</p>
 */
public interface UserQueryService {

    PageResult<UserResponse> list(UserPageQuery query);

    UserResponse getById(Long id);

    List<Long> getUserRoleIds(Long userId);
}
