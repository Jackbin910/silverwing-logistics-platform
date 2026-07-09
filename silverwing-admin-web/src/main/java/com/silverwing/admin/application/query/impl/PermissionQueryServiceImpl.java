package com.silverwing.admin.application.query.impl;

import com.silverwing.admin.application.query.PermissionQueryService;
import com.silverwing.biz.iam.domain.adapter.repository.PermissionRepository;
import com.silverwing.biz.iam.domain.model.aggregate.SysPermissionAggregate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 权限查询服务实现（CQRS 读侧）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionQueryServiceImpl implements PermissionQueryService {

    private final PermissionRepository permissionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<SysPermissionAggregate> listAll() {
        return permissionRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public SysPermissionAggregate getById(Long id) {
        return permissionRepository.findById(id);
    }
}
