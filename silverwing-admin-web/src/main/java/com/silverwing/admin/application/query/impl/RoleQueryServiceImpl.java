package com.silverwing.admin.application.query.impl;

import com.silverwing.admin.application.dto.RoleResponse;
import com.silverwing.admin.application.query.RolePageQuery;
import com.silverwing.admin.application.query.RoleQueryService;
import com.silverwing.admin.client.IamRoleClient;
import com.silverwing.common.domain.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 角色查询服务实现（CQRS 读侧）
 * <p>委托 {@link IamRoleClient} 防腐层端口完成查询，本类不再依赖 biz-iam 仓储与聚合根。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleQueryServiceImpl implements RoleQueryService {

    private final IamRoleClient iamRoleClient;

    @Override
    public PageResult<RoleResponse> list(RolePageQuery query) {
        return iamRoleClient.list(query);
    }

    @Override
    public List<RoleResponse> listAllEnabled() {
        return iamRoleClient.listAllEnabled();
    }

    @Override
    public RoleResponse getById(Long id) {
        return iamRoleClient.getById(id);
    }

    @Override
    public List<Long> getRolePermissionIds(Long roleId) {
        return iamRoleClient.getRolePermissionIds(roleId);
    }
}
