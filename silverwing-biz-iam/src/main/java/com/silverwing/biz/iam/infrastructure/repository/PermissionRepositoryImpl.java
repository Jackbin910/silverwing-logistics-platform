package com.silverwing.biz.iam.infrastructure.repository;

import com.alicp.jetcache.anno.Cached;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.silverwing.biz.iam.domain.model.SysPermission;
import com.silverwing.biz.iam.domain.repository.PermissionRepository;
import com.silverwing.biz.iam.infrastructure.mapper.SysPermissionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 权限仓储实现
 */
@Repository
@RequiredArgsConstructor
public class PermissionRepositoryImpl implements PermissionRepository {

    private final SysPermissionMapper sysPermissionMapper;

    @Override
    @Cached(name = "perm:id:", key = "#id", expire = 10, timeUnit = TimeUnit.MINUTES)
    public SysPermission findById(Long id) {
        return sysPermissionMapper.selectById(id);
    }

    @Override
    @Cached(name = "perm:all", expire = 10, timeUnit = TimeUnit.MINUTES)
    public List<SysPermission> findAll() {
        LambdaQueryWrapper<SysPermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(SysPermission::getSort);
        return sysPermissionMapper.selectList(wrapper);
    }

    @Override
    public void save(SysPermission permission) {
        if (permission.getId() != null) {
            sysPermissionMapper.updateById(permission);
        } else {
            sysPermissionMapper.insert(permission);
        }
    }

    @Override
    public void deleteById(Long id) {
        sysPermissionMapper.deleteById(id);
    }

    @Override
    @Cached(name = "perm:codes:byUser:", key = "#userId", expire = 5, timeUnit = TimeUnit.MINUTES)
    public List<String> findPermissionCodesByUserId(Long userId) {
        return sysPermissionMapper.selectPermissionCodesByUserId(userId);
    }
}
