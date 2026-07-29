package com.silverwing.biz.iam.infrastructure.adapter.repository;

import com.alicp.jetcache.anno.Cached;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.silverwing.biz.iam.domain.adapter.repository.PermissionRepository;
import com.silverwing.biz.iam.domain.model.aggregate.SysPermissionAggregate;
import com.silverwing.biz.iam.domain.model.query.PermissionQuery;
import com.silverwing.biz.iam.infrastructure.adapter.repository.convertor.PermissionInfraConvertor;
import com.silverwing.biz.iam.infrastructure.dao.SysPermissionDao;
import com.silverwing.biz.iam.infrastructure.dao.po.SysPermissionPO;
import com.silverwing.common.domain.PageResult;
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
    public PageResult<SysPermissionAggregate> findPage(PermissionQuery query) {
        query.normalize();
        Page<SysPermissionPO> pageObj = new Page<>(query.getCurrent(), query.getSize());
        LambdaQueryWrapper<SysPermissionPO> wrapper = new LambdaQueryWrapper<>();
        if (query.getKeyword() != null && !query.getKeyword().isBlank()) {
            String keyword = query.getKeyword();
            wrapper.and(w -> w.like(SysPermissionPO::getPermissionCode, keyword)
                    .or().like(SysPermissionPO::getPermissionName, keyword));
        }
        if (query.getStatus() != null) {
            wrapper.eq(SysPermissionPO::getStatus, query.getStatus());
        }
        wrapper.orderByAsc(SysPermissionPO::getSort);
        Page<SysPermissionPO> result = sysPermissionDao.selectPage(pageObj, wrapper);
        List<SysPermissionAggregate> records = result.getRecords().stream()
                .map(PermissionInfraConvertor.INSTANCE::toDomain)
                .toList();
        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), records);
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
