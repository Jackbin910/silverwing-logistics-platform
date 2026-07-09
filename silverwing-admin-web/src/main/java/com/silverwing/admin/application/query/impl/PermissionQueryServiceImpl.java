package com.silverwing.admin.application.query.impl;

import com.silverwing.admin.application.convertor.PermissionConvertor;
import com.silverwing.admin.application.dto.PermissionResponse;
import com.silverwing.admin.application.query.PermissionQueryService;
import com.silverwing.biz.iam.domain.adapter.repository.PermissionRepository;
import com.silverwing.biz.iam.domain.model.aggregate.SysPermissionAggregate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 权限查询服务实现（CQRS 读侧）
 * <p>从仓储获取聚合根后，统一经 PermissionConvertor 转换为 {@link PermissionResponse} 再返回。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionQueryServiceImpl implements PermissionQueryService {

    private final PermissionRepository permissionRepository;
    private final PermissionConvertor permissionConvertor;

    @Override
    @Transactional(readOnly = true)
    public List<PermissionResponse> listAll() {
        return permissionRepository.findAll().stream()
                .map(permissionConvertor::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PermissionResponse getById(Long id) {
        SysPermissionAggregate permission = permissionRepository.findById(id);
        return permission == null ? null : permissionConvertor.toResponse(permission);
    }
}
