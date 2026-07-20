package com.silverwing.biz.iam.domain.service.impl;

import com.silverwing.biz.iam.domain.adapter.repository.RoleRepository;
import com.silverwing.biz.iam.domain.model.aggregate.SysRoleAggregate;
import com.silverwing.biz.iam.domain.service.IRoleDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 角色领域服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleDomainServiceImpl implements IRoleDomainService {

    private final RoleRepository roleRepository;

    @Override
    public SysRoleAggregate registerRole(SysRoleAggregate role) {
        if (roleRepository.existsByRoleCode(role.getRoleCode())) {
            throw new IllegalArgumentException("角色编码已存在");
        }
        roleRepository.save(role);
        return role;
    }

    @Override
    public void assignPermissions(Long roleId, List<Long> permissionIds) {
        roleRepository.assignPermissions(roleId, permissionIds);
    }

    @Override
    public SysRoleAggregate update(SysRoleAggregate role) {
        roleRepository.save(role);
        return role;
    }

    @Override
    public void deleteById(Long id) {
        roleRepository.deleteById(id);
    }
}
