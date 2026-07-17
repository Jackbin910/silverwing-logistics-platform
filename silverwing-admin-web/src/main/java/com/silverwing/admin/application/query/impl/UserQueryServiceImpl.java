package com.silverwing.admin.application.query.impl;

import com.silverwing.admin.application.dto.UserResponse;
import com.silverwing.admin.application.query.UserPageQuery;
import com.silverwing.admin.application.query.UserQueryService;
import com.silverwing.admin.client.IamUserClient;
import com.silverwing.common.domain.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户查询服务实现（CQRS 读侧）
 * <p>委托 {@link IamUserClient} 防腐层端口完成查询，本类不再依赖 biz-iam 仓储与聚合根。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserQueryServiceImpl implements UserQueryService {

    private final IamUserClient iamUserClient;

    @Override
    public PageResult<UserResponse> list(UserPageQuery query) {
        return iamUserClient.list(query);
    }

    @Override
    public UserResponse getById(Long id) {
        return iamUserClient.getById(id);
    }

    @Override
    public List<Long> getUserRoleIds(Long userId) {
        return iamUserClient.getUserRoleIds(userId);
    }
}
