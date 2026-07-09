package com.silverwing.admin.application.query.impl;

import com.silverwing.admin.application.query.RoleQueryService;
import com.silverwing.biz.iam.domain.adapter.repository.RoleRepository;
import com.silverwing.biz.iam.domain.model.aggregate.SysRoleAggregate;
import com.silverwing.biz.iam.domain.model.query.RoleQuery;
import com.silverwing.common.domain.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 角色查询服务实现（CQRS 读侧）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleQueryServiceImpl implements RoleQueryService {

    private final RoleRepository roleRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResult<SysRoleAggregate> list(RoleQuery query) {
        return roleRepository.findPage(query);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SysRoleAggregate> listAllEnabled() {
        return roleRepository.findAllEnabled();
    }

    @Override
    @Transactional(readOnly = true)
    public SysRoleAggregate getById(Long id) {
        return roleRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getRolePermissionIds(Long roleId) {
        return roleRepository.findPermissionIdsByRoleId(roleId);
    }
}
