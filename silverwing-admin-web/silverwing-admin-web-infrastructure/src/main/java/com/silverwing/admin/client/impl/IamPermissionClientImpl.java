package com.silverwing.admin.client.impl;

import com.silverwing.admin.application.command.SavePermissionCommand;
import com.silverwing.admin.application.dto.PermissionResponse;
import com.silverwing.admin.client.IamPermissionClient;
import com.silverwing.admin.client.convertor.PermissionConvertor;
import com.silverwing.biz.iam.domain.adapter.repository.PermissionRepository;
import com.silverwing.biz.iam.domain.model.aggregate.SysPermissionAggregate;
import com.silverwing.biz.iam.domain.service.IPermissionDomainService;
import com.silverwing.common.domain.ResultCode;
import com.silverwing.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * IAM 权限上下文防腐层适配器
 * <p>本类是唯一直接依赖 biz-iam 权限领域层（聚合根、仓储、领域服务）的地方。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IamPermissionClientImpl implements IamPermissionClient {

    private final PermissionRepository permissionRepository;
    private final PermissionConvertor permissionConvertor;
    private final IPermissionDomainService permissionDomainService;

    @Override
    @Transactional
    public PermissionResponse create(SavePermissionCommand command) {
        SysPermissionAggregate permission = new SysPermissionAggregate();
        permissionConvertor.applyCommandToEntity(permission, command);
        permission.enable();
        // 领域服务负责权限持久化
        permission = permissionDomainService.save(permission);
        log.info("新建权限成功 permissionCode={}, id={}",
                command.getPermissionCode(), permission.getId());
        return permissionConvertor.toResponse(permission);
    }

    @Override
    @Transactional
    public void update(Long id, SavePermissionCommand command) {
        SysPermissionAggregate permission = permissionRepository.findById(id);
        if (permission == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "权限不存在");
        }
        permissionConvertor.applyCommandToEntity(permission, command);
        // 领域服务负责权限持久化
        permissionDomainService.save(permission);
        log.info("更新权限 id={}", id);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        // 领域服务负责删除
        permissionDomainService.deleteById(id);
        log.info("删除权限 id={}", id);
    }

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
