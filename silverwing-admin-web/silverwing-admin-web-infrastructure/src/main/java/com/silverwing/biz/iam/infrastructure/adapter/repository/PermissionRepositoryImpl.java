package com.silverwing.biz.iam.infrastructure.adapter.repository;

import com.alicp.jetcache.anno.Cached;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.silverwing.biz.iam.domain.adapter.repository.PermissionRepository;
import com.silverwing.biz.iam.domain.model.aggregate.SysPermissionAggregate;
import com.silverwing.biz.iam.infrastructure.adapter.repository.convertor.PermissionInfraConvertor;
import com.silverwing.biz.iam.infrastructure.dao.SysPermissionDao;
import com.silverwing.biz.iam.infrastructure.dao.po.SysPermissionPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 权限仓储实现（基础设施适配器）
 */
@Repository
@RequiredArgsConstructor
public class PermissionRepositoryImpl implements PermissionRepository {

    private final SysPermissionDao sysPermissionDao;

    @Override
    @Cached(name = "perm:id:", key = "#id", expire = 10, timeUnit = TimeUnit.MINUTES)
    public SysPermissionAggregate findById(Long id) {
        return PermissionInfraConvertor.INSTANCE.toDomain(sysPermissionDao.selectById(id));
    }

    @Override
    @Cached(name = "perm:all", expire = 10, timeUnit = TimeUnit.MINUTES)
    public List<SysPermissionAggregate> findAll() {
        LambdaQueryWrapper<SysPermissionPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(SysPermissionPO::getSort);
        return sysPermissionDao.selectList(wrapper).stream()
                .map(PermissionInfraConvertor.INSTANCE::toDomain)
                .toList();
    }

    @Override
    public void save(SysPermissionAggregate permission) {
        SysPermissionPO po = PermissionInfraConvertor.INSTANCE.toPo(permission);
        if (permission.getId() != null) {
            sysPermissionDao.updateById(po);
        } else {
            sysPermissionDao.insert(po);
        }
    }

    @Override
    public void deleteById(Long id) {
        sysPermissionDao.deleteById(id);
    }

    @Override
    @Cached(name = "perm:codes:byUser:", key = "#userId", expire = 5, timeUnit = TimeUnit.MINUTES)
    public List<String> findPermissionCodesByUserId(Long userId) {
        return sysPermissionDao.selectPermissionCodesByUserId(userId);
    }
}
