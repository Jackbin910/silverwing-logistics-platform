package com.silverwing.common.infrastructure.repository;

import com.alicp.jetcache.anno.Cached;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.silverwing.common.domain.PageResult;
import com.silverwing.common.domain.model.RoleQuery;
import com.silverwing.common.domain.model.SysRole;
import com.silverwing.common.domain.model.SysRolePermission;
import com.silverwing.common.domain.model.SysUserRole;
import com.silverwing.common.domain.repository.RoleRepository;
import com.silverwing.common.infrastructure.mapper.SysRoleMapper;
import com.silverwing.common.infrastructure.mapper.SysRolePermissionMapper;
import com.silverwing.common.infrastructure.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 角色仓储实现
 */
@Repository
@RequiredArgsConstructor
public class RoleRepositoryImpl implements RoleRepository {

    private final SysRoleMapper sysRoleMapper;
    private final SysRolePermissionMapper sysRolePermissionMapper;
    private final SysUserRoleMapper sysUserRoleMapper;

    @Override
    @Cached(name = "role:id:", key = "#id", expire = 10, timeUnit = TimeUnit.MINUTES)
    public SysRole findById(Long id) {
        return sysRoleMapper.selectById(id);
    }

    @Override
    public SysRole findByRoleCode(String roleCode) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getRoleCode, roleCode);
        return sysRoleMapper.selectOne(wrapper);
    }

    @Override
    public boolean existsByRoleCode(String roleCode) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getRoleCode, roleCode);
        return sysRoleMapper.selectCount(wrapper) > 0;
    }

    @Override
    public void save(SysRole role) {
        if (role.getId() != null) {
            sysRoleMapper.updateById(role);
        } else {
            sysRoleMapper.insert(role);
        }
    }

    @Override
    public void deleteById(Long id) {
        sysRoleMapper.deleteById(id);
        LambdaQueryWrapper<SysRolePermission> rpWrapper = new LambdaQueryWrapper<>();
        rpWrapper.eq(SysRolePermission::getRoleId, id);
        sysRolePermissionMapper.delete(rpWrapper);
        LambdaQueryWrapper<SysUserRole> urWrapper = new LambdaQueryWrapper<>();
        urWrapper.eq(SysUserRole::getRoleId, id);
        sysUserRoleMapper.delete(urWrapper);
    }

    @Override
    public PageResult<SysRole> findPage(RoleQuery query) {
        query.normalize();
        Page<SysRole> pageObj = new Page<>(query.getCurrent(), query.getSize());
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        if (query.getRoleName() != null && !query.getRoleName().isBlank()) {
            wrapper.like(SysRole::getRoleName, query.getRoleName());
        }
        if (query.getStatus() != null) {
            wrapper.eq(SysRole::getStatus, query.getStatus());
        }
        wrapper.orderByDesc(SysRole::getCreateTime);
        Page<SysRole> result = sysRoleMapper.selectPage(pageObj, wrapper);
        return new PageResult<>(result.getCurrent(), result.getSize(),
                result.getTotal(), result.getRecords());
    }

    @Override
    @Cached(name = "role:allEnabled", expire = 10, timeUnit = TimeUnit.MINUTES)
    public List<SysRole> findAllEnabled() {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getStatus, 1);
        wrapper.orderByAsc(SysRole::getId);
        return sysRoleMapper.selectList(wrapper);
    }

    @Override
    @Cached(name = "role:byUser:", key = "#userId", expire = 5, timeUnit = TimeUnit.MINUTES)
    public List<SysRole> findRolesByUserId(Long userId) {
        return sysUserRoleMapper.selectRolesByUserId(userId);
    }

    @Override
    public List<Long> findUserIdsByRoleId(Long roleId) {
        return sysUserRoleMapper.selectUserIdsByRoleId(roleId);
    }

    @Override
    public void assignPermissions(Long roleId, List<Long> permissionIds) {
        LambdaQueryWrapper<SysRolePermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRolePermission::getRoleId, roleId);
        sysRolePermissionMapper.delete(wrapper);
        if (permissionIds != null) {
            for (Long permissionId : permissionIds) {
                sysRolePermissionMapper.insert(SysRolePermission.of(roleId, permissionId));
            }
        }
    }

    @Override
    public List<Long> findPermissionIdsByRoleId(Long roleId) {
        LambdaQueryWrapper<SysRolePermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRolePermission::getRoleId, roleId);
        return sysRolePermissionMapper.selectList(wrapper).stream()
                .map(SysRolePermission::getPermissionId)
                .toList();
    }
}
