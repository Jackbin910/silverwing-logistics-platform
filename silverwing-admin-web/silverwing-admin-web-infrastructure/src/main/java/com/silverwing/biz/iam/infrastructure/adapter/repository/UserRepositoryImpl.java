package com.silverwing.biz.iam.infrastructure.adapter.repository;

import com.alicp.jetcache.anno.Cached;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.silverwing.biz.iam.domain.adapter.repository.UserRepository;
import com.silverwing.biz.iam.domain.model.aggregate.SysUserAggregate;
import com.silverwing.biz.iam.domain.model.entity.SysUserRole;
import com.silverwing.biz.iam.domain.model.query.UserQuery;
import com.silverwing.biz.iam.infrastructure.adapter.repository.convertor.RelationInfraConvertor;
import com.silverwing.biz.iam.infrastructure.adapter.repository.convertor.UserInfraConvertor;
import com.silverwing.biz.iam.infrastructure.dao.SysUserDao;
import com.silverwing.biz.iam.infrastructure.dao.SysUserRoleDao;
import com.silverwing.biz.iam.infrastructure.dao.po.SysUserPO;
import com.silverwing.biz.iam.infrastructure.dao.po.SysUserRolePO;
import com.silverwing.common.domain.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 用户仓储实现（基础设施适配器）
 * <p>通过 DAO 操作 PO，并经 InfraConvertor 与领域实体互转，落实防腐转换。</p>
 */
@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final SysUserDao sysUserDao;
    private final SysUserRoleDao sysUserRoleDao;

    @Override
    @Cached(name = "user:id:", key = "#id", expire = 5, timeUnit = TimeUnit.MINUTES)
    public SysUserAggregate findById(Long id) {
        return UserInfraConvertor.INSTANCE.toDomain(sysUserDao.selectById(id));
    }

    @Override
    @Cached(name = "user:username:", key = "#username", expire = 5, timeUnit = TimeUnit.MINUTES)
    public SysUserAggregate findByUsername(String username) {
        LambdaQueryWrapper<SysUserPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserPO::getUsername, username);
        return UserInfraConvertor.INSTANCE.toDomain(sysUserDao.selectOne(wrapper));
    }

    @Override
    public boolean existsByUsername(String username) {
        LambdaQueryWrapper<SysUserPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserPO::getUsername, username);
        return sysUserDao.selectCount(wrapper) > 0;
    }

    @Override
    public void save(SysUserAggregate user) {
        SysUserPO po = UserInfraConvertor.INSTANCE.toPo(user);
        if (user.getId() != null) {
            sysUserDao.updateById(po);
        } else {
            sysUserDao.insert(po);
        }
    }

    @Override
    public void deleteById(Long id) {
        sysUserDao.deleteById(id);
        LambdaQueryWrapper<SysUserRolePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRolePO::getUserId, id);
        sysUserRoleDao.delete(wrapper);
    }

    @Override
    public PageResult<SysUserAggregate> findPage(UserQuery query) {
        query.normalize();
        Page<SysUserPO> pageObj = new Page<>(query.getCurrent(), query.getSize());
        LambdaQueryWrapper<SysUserPO> wrapper = new LambdaQueryWrapper<>();
        if (query.getUsername() != null && !query.getUsername().isBlank()) {
            wrapper.like(SysUserPO::getUsername, query.getUsername());
        }
        if (query.getStatus() != null) {
            wrapper.eq(SysUserPO::getStatus, query.getStatus());
        }
        wrapper.orderByDesc(SysUserPO::getCreateTime);
        Page<SysUserPO> result = sysUserDao.selectPage(pageObj, wrapper);
        List<SysUserAggregate> records = result.getRecords().stream()
                .map(UserInfraConvertor.INSTANCE::toDomain)
                .toList();
        records.forEach(SysUserAggregate::clearPassword);
        return new PageResult<>(result.getCurrent(), result.getSize(),
                result.getTotal(), records);
    }

    @Override
    public List<Long> findRoleIdsByUserId(Long userId) {
        LambdaQueryWrapper<SysUserRolePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRolePO::getUserId, userId);
        return sysUserRoleDao.selectList(wrapper).stream()
                .map(RelationInfraConvertor.INSTANCE::toUserRoleDomain)
                .map(SysUserRole::getRoleId)
                .toList();
    }

    @Override
    public void assignRoles(Long userId, List<Long> roleIds) {
        LambdaQueryWrapper<SysUserRolePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRolePO::getUserId, userId);
        sysUserRoleDao.delete(wrapper);
        if (roleIds != null) {
            for (Long roleId : roleIds) {
                sysUserRoleDao.insert(RelationInfraConvertor.INSTANCE.toUserRolePo(SysUserRole.of(userId, roleId)));
            }
        }
    }

    @Override
    public void deleteUserRoles(Long userId) {
        LambdaQueryWrapper<SysUserRolePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRolePO::getUserId, userId);
        sysUserRoleDao.delete(wrapper);
    }
}
