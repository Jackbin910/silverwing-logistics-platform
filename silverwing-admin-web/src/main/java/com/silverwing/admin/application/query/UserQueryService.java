package com.silverwing.admin.application.query;

import com.silverwing.biz.iam.domain.model.aggregate.SysUserAggregate;
import com.silverwing.biz.iam.domain.model.query.UserQuery;
import com.silverwing.common.domain.PageResult;

import java.util.List;

/**
 * 用户查询服务（CQRS 读侧）
 * <p>定义用户只读用例的端口，实现见 {@code impl} 包。</p>
 */
public interface UserQueryService {

    PageResult<SysUserAggregate> list(UserQuery query);

    SysUserAggregate getById(Long id);

    List<Long> getUserRoleIds(Long userId);
}
