package com.silverwing.admin.application.query.impl;

import com.silverwing.admin.application.dto.PermissionResponse;
import com.silverwing.admin.application.dto.RolePermissionTreeSelectResponse;
import com.silverwing.admin.application.dto.RouterVo;
import com.silverwing.admin.application.dto.TreeSelect;
import com.silverwing.admin.application.query.PermissionPageQuery;
import com.silverwing.admin.application.query.PermissionQueryService;
import com.silverwing.admin.client.IamPermissionClient;
import com.silverwing.common.domain.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 权限查询服务实现（CQRS 读侧）
 * <p>委托 {@link IamPermissionClient} 防腐层端口完成查询，本类不再依赖 biz-iam 仓储与聚合根。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionQueryServiceImpl implements PermissionQueryService {

    private final IamPermissionClient iamPermissionClient;

    @Override
    public List<PermissionResponse> listAll() {
        return iamPermissionClient.listAll();
    }

    @Override
    public PageResult<PermissionResponse> page(PermissionPageQuery query) {
        return iamPermissionClient.page(query);
    }

    @Override
    public PermissionResponse getById(Long id) {
        return iamPermissionClient.getById(id);
    }

    @Override
    public List<TreeSelect> treeSelect(PermissionPageQuery query) {
        return iamPermissionClient.treeSelect(query);
    }

    @Override
    public RolePermissionTreeSelectResponse rolePermissionTreeSelect(Long roleId) {
        return iamPermissionClient.rolePermissionTreeSelect(roleId);
    }

    @Override
    public List<RouterVo> getRouters(Long userId) {
        return iamPermissionClient.getRouters(userId);
    }
}
