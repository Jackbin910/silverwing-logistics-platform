package com.silverwing.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.silverwing.auth.entity.SysRole;
import com.silverwing.auth.entity.SysUserRole;
import com.silverwing.auth.mapper.SysRoleMapper;
import com.silverwing.auth.mapper.SysUserRoleMapper;
import com.silverwing.auth.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 角色服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl implements SysRoleService {

    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;

    @Override
    public List<SysRole> getRolesByUserId(Long userId) {
        return sysUserRoleMapper.selectRolesByUserId(userId);
    }

    @Override
    public SysRole getByRoleCode(String roleCode) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getRoleCode, roleCode);
        return sysRoleMapper.selectOne(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRoleToUser(Long userId, Long roleId) {
        // 检查是否已经分配
        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRole::getUserId, userId)
               .eq(SysUserRole::getRoleId, roleId);
        
        Long count = sysUserRoleMapper.selectCount(wrapper);
        if (count > 0) {
            log.warn("用户已拥有该角色 userId={}, roleId={}", userId, roleId);
            return;
        }

        // 分配角色
        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(roleId);
        sysUserRoleMapper.insert(userRole);
        
        log.info("为用户分配角色成功 userId={}, roleId={}", userId, roleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeRoleFromUser(Long userId, Long roleId) {
        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRole::getUserId, userId)
               .eq(SysUserRole::getRoleId, roleId);
        
        sysUserRoleMapper.delete(wrapper);
        log.info("移除用户角色 userId={}, roleId={}", userId, roleId);
    }
}
