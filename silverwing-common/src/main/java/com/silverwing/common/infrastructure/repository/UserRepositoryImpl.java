package com.silverwing.common.infrastructure.repository;

import com.alicp.jetcache.anno.Cached;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.silverwing.common.domain.PageResult;
import com.silverwing.common.domain.model.SysUser;
import com.silverwing.common.domain.model.SysUserRole;
import com.silverwing.common.domain.model.UserQuery;
import com.silverwing.common.domain.repository.UserRepository;
import com.silverwing.common.infrastructure.mapper.SysUserMapper;
import com.silverwing.common.infrastructure.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 用户仓储实现
 */
@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final SysUserMapper sysUserMapper;
    private final SysUserRoleMapper sysUserRoleMapper;

    @Override
    @Cached(name = "user:id:", key = "#id", expire = 5, timeUnit = TimeUnit.MINUTES)
    public SysUser findById(Long id) {
        return sysUserMapper.selectById(id);
    }

    @Override
    @Cached(name = "user:username:", key = "#username", expire = 5, timeUnit = TimeUnit.MINUTES)
    public SysUser findByUsername(String username) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username);
        return sysUserMapper.selectOne(wrapper);
    }

    @Override
    public boolean existsByUsername(String username) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username);
        return sysUserMapper.selectCount(wrapper) > 0;
    }

    @Override
    public void save(SysUser user) {
        if (user.getId() != null) {
            sysUserMapper.updateById(user);
        } else {
            sysUserMapper.insert(user);
        }
    }

    @Override
    public void deleteById(Long id) {
        sysUserMapper.deleteById(id);
        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRole::getUserId, id);
        sysUserRoleMapper.delete(wrapper);
    }

    @Override
    public PageResult<SysUser> findPage(UserQuery query) {
        query.normalize();
        Page<SysUser> pageObj = new Page<>(query.getCurrent(), query.getSize());
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (query.getUsername() != null && !query.getUsername().isBlank()) {
            wrapper.like(SysUser::getUsername, query.getUsername());
        }
        if (query.getStatus() != null) {
            wrapper.eq(SysUser::getStatus, query.getStatus());
        }
        wrapper.orderByDesc(SysUser::getCreateTime);
        Page<SysUser> result = sysUserMapper.selectPage(pageObj, wrapper);
        result.getRecords().forEach(SysUser::clearPassword);
        return new PageResult<>(result.getCurrent(), result.getSize(),
                result.getTotal(), result.getRecords());
    }

    @Override
    public List<Long> findRoleIdsByUserId(Long userId) {
        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRole::getUserId, userId);
        return sysUserRoleMapper.selectList(wrapper).stream()
                .map(SysUserRole::getRoleId)
                .toList();
    }

    @Override
    public void assignRoles(Long userId, List<Long> roleIds) {
        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRole::getUserId, userId);
        sysUserRoleMapper.delete(wrapper);
        if (roleIds != null) {
            for (Long roleId : roleIds) {
                sysUserRoleMapper.insert(SysUserRole.of(userId, roleId));
            }
        }
    }

    @Override
    public void deleteUserRoles(Long userId) {
        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRole::getUserId, userId);
        sysUserRoleMapper.delete(wrapper);
    }
}
