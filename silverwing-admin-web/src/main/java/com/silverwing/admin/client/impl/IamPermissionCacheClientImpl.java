package com.silverwing.admin.client.impl;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import com.silverwing.admin.client.IamPermissionCacheClient;
import com.silverwing.biz.iam.domain.adapter.repository.PermissionRepository;
import com.silverwing.biz.iam.domain.adapter.repository.RoleRepository;
import com.silverwing.biz.iam.domain.model.aggregate.SysRoleAggregate;
import com.silverwing.common.constant.SaSessionConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 权限缓存防腐层适配器
 * <p>封装 Sa-Token Session 权限/角色缓存刷新，隔离应用层对 Sa-Token 与 biz-iam 仓储的直接依赖。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IamPermissionCacheClientImpl implements IamPermissionCacheClient {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;

    @Override
    public void refreshUserPermissionCache(Long userId) {
        try {
            List<String> permissionCodes = permissionRepository.findPermissionCodesByUserId(userId);
            List<SysRoleAggregate> roles = roleRepository.findRolesByUserId(userId);
            List<String> roleCodes = roles.stream()
                    .map(SysRoleAggregate::getRoleCode)
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

    @Override
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
}
