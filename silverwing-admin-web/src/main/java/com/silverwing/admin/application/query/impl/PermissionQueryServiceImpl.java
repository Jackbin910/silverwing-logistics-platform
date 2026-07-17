package com.silverwing.admin.application.query.impl;

import com.silverwing.admin.application.dto.PermissionResponse;
import com.silverwing.admin.application.query.PermissionQueryService;
import com.silverwing.admin.client.IamPermissionClient;
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
    public PermissionResponse getById(Long id) {
        return iamPermissionClient.getById(id);
    }
}
