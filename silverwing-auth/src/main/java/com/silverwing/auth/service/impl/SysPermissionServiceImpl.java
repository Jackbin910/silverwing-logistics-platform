package com.silverwing.auth.service.impl;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.silverwing.auth.entity.SysPermission;
import com.silverwing.auth.entity.SysRole;
import com.silverwing.auth.entity.SysRolePermission;
import com.silverwing.auth.mapper.SysPermissionMapper;
import com.silverwing.auth.mapper.SysRolePermissionMapper;
import com.silverwing.auth.mapper.SysUserRoleMapper;
import com.silverwing.auth.service.SysPermissionService;
import com.silverwing.auth.service.SysRoleService;
import com.silverwing.common.constant.SaSessionConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 权限服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysPermissionServiceImpl implements SysPermissionService {

    private final SysPermissionMapper sysPermissionMapper;
    private final SysRolePermissionMapper sysRolePermissionMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRoleService sysRoleService;

    @Override
    public List<String> getPermissionCodesByUserId(Long userId) {
        return sysPermissionMapper.selectPermissionCodesByUserId(userId);
    }

    @Override
    public List<SysPermission> listAll() {
        LambdaQueryWrapper<SysPermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(SysPermission::getSort);
        return sysPermissionMapper.selectList(wrapper);
    }

    @Override
    public List<Long> getPermissionIdsByRoleId(Long roleId) {
        LambdaQueryWrapper<SysRolePermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRolePermission::getRoleId, roleId);
        return sysRolePermissionMapper.selectList(wrapper).stream()
                .map(SysRolePermission::getPermissionId)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignPermissionsToRole(Long roleId, List<Long> permissionIds) {
        // 1. 先删除该角色的全部权限关联
        LambdaQueryWrapper<SysRolePermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRolePermission::getRoleId, roleId);
        sysRolePermissionMapper.delete(wrapper);

        // 2. 批量插入新的权限关联
        if (permissionIds != null && !permissionIds.isEmpty()) {
            for (Long permissionId : permissionIds) {
                SysRolePermission rp = new SysRolePermission();
                rp.setRoleId(roleId);
                rp.setPermissionId(permissionId);
                sysRolePermissionMapper.insert(rp);
            }
        }

        // 3. 刷新该角色下所有在线用户的权限缓存
        try {
            List<Long> userIds = sysUserRoleMapper.selectUserIdsByRoleId(roleId);
            for (Long userId : userIds) {
                if (StpUtil.isLogin(userId)) {
                    refreshUserPermissionCache(userId);
                }
            }
            log.info("角色权限分配完成 roleId={}, 权限数={}", roleId, permissionIds == null ? 0 : permissionIds.size());
        } catch (Exception e) {
            // 缓存刷新失败不影响主流程，用户重新登录后会自动加载最新权限
            log.warn("刷新角色在线用户权限缓存失败 roleId={}：{}", roleId, e.getMessage());
        }
    }

    @Override
    public void refreshUserPermissionCache(Long userId) {
        try {
            // 重新查询权限与角色
            List<String> permissionCodes = getPermissionCodesByUserId(userId);
            List<SysRole> roles = sysRoleService.getRolesByUserId(userId);
            List<String> roleCodes = roles.stream()
                    .map(SysRole::getRoleCode)
                    .collect(Collectors.toList());

            // 写入 Sa-Token Session（Redis 共享），各微服务通过 StpInterface 读取
            SaSession session = StpUtil.getSessionByLoginId(userId);
            session.set(SaSessionConstants.PERMISSION_LIST, permissionCodes);
            session.set(SaSessionConstants.ROLE_LIST, roleCodes);

            log.info("刷新用户权限缓存成功 userId={}, 权限数={}, 角色数={}",
                    userId, permissionCodes.size(), roleCodes.size());
        } catch (Exception e) {
            log.warn("刷新用户权限缓存失败 userId={}：{}", userId, e.getMessage());
        }
    }

}
