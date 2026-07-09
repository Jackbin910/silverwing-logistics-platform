package com.silverwing.biz.iam.domain.service.impl;

import com.silverwing.biz.iam.domain.adapter.repository.PermissionRepository;
import com.silverwing.biz.iam.domain.model.aggregate.SysPermissionAggregate;
import com.silverwing.biz.iam.domain.service.IPermissionDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 权限领域服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionDomainServiceImpl implements IPermissionDomainService {

    private final PermissionRepository permissionRepository;

    @Override
    public SysPermissionAggregate save(SysPermissionAggregate permission) {
        permissionRepository.save(permission);
        return permission;
    }

    @Override
    public void deleteById(Long id) {
        permissionRepository.deleteById(id);
    }
}
