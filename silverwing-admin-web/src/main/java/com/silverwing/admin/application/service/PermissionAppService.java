package com.silverwing.admin.application.service;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import com.silverwing.admin.application.command.SavePermissionCommand;
import com.silverwing.biz.iam.domain.model.SysPermission;
import com.silverwing.biz.iam.domain.model.SysRole;
import com.silverwing.biz.iam.domain.repository.PermissionRepository;
import com.silverwing.biz.iam.domain.repository.RoleRepository;
import com.silverwing.common.constant.SaSessionConstants;
import com.silverwing.common.domain.ResultCode;
import com.silverwing.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 权限管理应用服务
 * <p>
 * 包含权限 CRUD 和 Sa-Token Session 缓存刷新。
 * 缓存刷新通过 Redis 共享 Session 直接操作，无需跨服务调用。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionAppService {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;

    @Transactional(readOnly = true)
    public List<SysPermission> listAll() {
        return permissionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public SysPermission getById(Long id) {
        return permissionRepository.findById(id);
    }

    @Transactional
    public SysPermission create(SavePermissionCommand command) {
        SysPermission permission = new SysPermission();
        applyCommandToEntity(permission, command);
        permission.enable();

        permissionRepository.save(permission);
        log.info("新建权限成功 permissionCode={}, id={}",
                command.getPermissionCode(), permission.getId());
        return permission;
    }

    @Transactional
    public void update(Long id, SavePermissionCommand command) {
        SysPermission permission = permissionRepository.findById(id);
        if (permission == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "权限不存在");
        }
        applyCommandToEntity(permission, command);
        permissionRepository.save(permission);
        log.info("更新权限 id={}", id);
    }

    @Transactional
    public void delete(Long id) {
        permissionRepository.deleteById(id);
        log.info("删除权限 id={}", id);
    }

    /**
     * 刷新指定用户的权限缓存
     */
    public void refreshUserPermissionCache(Long userId) {
        try {
            List<String> permissionCodes = permissionRepository.findPermissionCodesByUserId(userId);
            List<SysRole> roles = roleRepository.findRolesByUserId(userId);
            List<String> roleCodes = roles.stream()
                    .map(SysRole::getRoleCode)
                    .collect(Collectors.toList());

            SaSession session = StpUtil.getSessionByLoginId(userId);
            session.set(SaSessionConstants.PERMISSION_LIST, permissionCodes);
            session.set(SaSessionConstants.ROLE_LIST, roleCodes);

            log.info("刷新用户权限缓存成功 userId={}, 权限数={}, 角色数={}",
                    userId, permissionCodes.size(), roleCodes.size());
        } catch (Exception e) {
            log.warn("刷新用户权限缓存失败 userId={}：{}", userId, e.getMessage());
        }
    }

    /**
     * 批量刷新角色下所有在线用户的权限缓存
     */
    public void refreshRoleUserCache(Long roleId) {
        try {
            List<Long> userIds = roleRepository.findUserIdsByRoleId(roleId);
            for (Long userId : userIds) {
                if (StpUtil.isLogin(userId)) {
                    refreshUserPermissionCache(userId);
                }
            }
            log.info("批量刷新角色用户缓存 roleId={}, 用户数={}", roleId, userIds.size());
        } catch (Exception e) {
            log.warn("批量刷新角色用户缓存失败 roleId={}：{}", roleId, e.getMessage());
        }
    }

    private void applyCommandToEntity(SysPermission entity, SavePermissionCommand cmd) {
        if (cmd.getPermissionCode() != null) {
            entity.setPermissionCode(cmd.getPermissionCode());
        }
        if (cmd.getPermissionName() != null) {
            entity.setPermissionName(cmd.getPermissionName());
        }
        if (cmd.getResourceType() != null) {
            entity.setResourceType(cmd.getResourceType());
        }
        entity.setParentId(cmd.getParentId() != null ? cmd.getParentId() : 0L);
        entity.setSort(cmd.getSort() != null ? cmd.getSort() : 0);
        if (cmd.getStatus() != null) {
            if (cmd.getStatus() == 1) entity.enable();
            else entity.disable();
        }
        entity.setVisible(cmd.getVisible());
        if (cmd.getUrl() != null) {
            entity.setUrl(cmd.getUrl());
        }
        if (cmd.getTarget() != null) {
            entity.setTarget(cmd.getTarget());
        }
        entity.setIsRefresh(cmd.getIsRefresh());
        if (cmd.getIcon() != null) {
            entity.setIcon(cmd.getIcon());
        }
    }
}
