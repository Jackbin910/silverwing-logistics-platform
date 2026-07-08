package com.silverwing.admin.application.service;

import com.silverwing.admin.application.command.SaveRoleCommand;
import com.silverwing.common.domain.PageResult;
import com.silverwing.common.domain.ResultCode;
import com.silverwing.common.domain.model.SysRole;
import com.silverwing.common.domain.model.RoleQuery;
import com.silverwing.common.domain.repository.RoleRepository;
import com.silverwing.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 角色管理应用服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleAppService {

    private final RoleRepository roleRepository;

    @Transactional(readOnly = true)
    public PageResult<SysRole> list(RoleQuery query) {
        return roleRepository.findPage(query);
    }

    @Transactional(readOnly = true)
    public List<SysRole> listAllEnabled() {
        return roleRepository.findAllEnabled();
    }

    @Transactional(readOnly = true)
    public SysRole getById(Long id) {
        return roleRepository.findById(id);
    }

    @Transactional
    public SysRole create(SaveRoleCommand command) {
        if (roleRepository.existsByRoleCode(command.getRoleCode())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "角色编码已存在");
        }

        SysRole role = new SysRole();
        role.setRoleCode(command.getRoleCode());
        role.setRoleName(command.getRoleName());
        if (command.getStatus() != null) {
            if (command.getStatus() == 1) role.enable();
            else role.disable();
        } else {
            role.enable();
        }

        roleRepository.save(role);
        log.info("新建角色成功 roleCode={}, id={}", role.getRoleCode(), role.getId());
        return role;
    }

    @Transactional
    public void update(Long id, SaveRoleCommand command) {
        SysRole role = roleRepository.findById(id);
        if (role == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "角色不存在");
        }

        if (command.getRoleCode() != null) role.setRoleCode(command.getRoleCode());
        if (command.getRoleName() != null) role.setRoleName(command.getRoleName());
        if (command.getStatus() != null) {
            if (command.getStatus() == 1) role.enable();
            else role.disable();
        }

        roleRepository.save(role);
        log.info("更新角色 id={}", id);
    }

    @Transactional
    public void delete(Long id) {
        roleRepository.deleteById(id);
        log.info("删除角色 id={}, 已级联清理关联数据", id);
    }

    @Transactional
    public void assignPermissions(Long roleId, List<Long> permissionIds) {
        roleRepository.assignPermissions(roleId, permissionIds);
        log.info("分配角色权限 roleId={}, 权限数={}", roleId,
                permissionIds == null ? 0 : permissionIds.size());
    }

    @Transactional(readOnly = true)
    public List<Long> getRolePermissionIds(Long roleId) {
        return roleRepository.findPermissionIdsByRoleId(roleId);
    }
}
