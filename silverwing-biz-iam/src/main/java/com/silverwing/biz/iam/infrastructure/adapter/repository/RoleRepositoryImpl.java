package com.silverwing.biz.iam.infrastructure.adapter.repository;

import com.alicp.jetcache.anno.Cached;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.silverwing.biz.iam.domain.adapter.repository.RoleRepository;
import com.silverwing.biz.iam.domain.model.aggregate.SysRoleAggregate;
import com.silverwing.biz.iam.domain.model.entity.SysRolePermission;
import com.silverwing.biz.iam.domain.model.entity.SysUserRole;
import com.silverwing.biz.iam.domain.model.query.RoleQuery;
import com.silverwing.biz.iam.infrastructure.adapter.repository.convertor.RelationInfraConvertor;
import com.silverwing.biz.iam.infrastructure.adapter.repository.convertor.RoleInfraConvertor;
import com.silverwing.biz.iam.infrastructure.dao.SysRoleDao;
import com.silverwing.biz.iam.infrastructure.dao.SysRolePermissionDao;
import com.silverwing.biz.iam.infrastructure.dao.SysUserRoleDao;
import com.silverwing.biz.iam.infrastructure.dao.po.SysRolePO;
import com.silverwing.biz.iam.infrastructure.dao.po.SysRolePermissionPO;
import com.silverwing.biz.iam.infrastructure.dao.po.SysUserRolePO;
import com.silverwing.common.domain.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 角色仓储实现（基础设施适配器）
 */
@Repository
@RequiredArgsConstructor
public class RoleRepositoryImpl implements RoleRepository {

    private final SysRoleDao sysRoleDao;
    private final SysRolePermissionDao sysRolePermissionDao;
    private final SysUserRoleDao sysUserRoleDao;

    @Override
    @Cached(name = "role:id:", key = "#id", expire = 10, timeUnit = TimeUnit.MINUTES)
    public SysRoleAggregate findById(Long id) {
        return RoleInfraConvertor.INSTANCE.toDomain(sysRoleDao.selectById(id));
    }

    @Override
    public SysRoleAggregate findByRoleCode(String roleCode) {
        LambdaQueryWrapper<SysRolePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRolePO::getRoleCode, roleCode);
        return RoleInfraConvertor.INSTANCE.toDomain(sysRoleDao.selectOne(wrapper));
    }

    @Override
    public boolean existsByRoleCode(String roleCode) {
        LambdaQueryWrapper<SysRolePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRolePO::getRoleCode, roleCode);
        return sysRoleDao.selectCount(wrapper) > 0;
    }

    @Override
    public void save(SysRoleAggregate role) {
        SysRolePO po = RoleInfraConvertor.INSTANCE.toPo(role);
        if (role.getId() != null) {
            sysRoleDao.updateById(po);
        } else {
            sysRoleDao.insert(po);
        }
    }

    @Override
    public void deleteById(Long id) {
        sysRoleDao.deleteById(id);
        LambdaQueryWrapper<SysRolePermissionPO> rpWrapper = new LambdaQueryWrapper<>();
        rpWrapper.eq(SysRolePermissionPO::getRoleId, id);
        sysRolePermissionDao.delete(rpWrapper);
        LambdaQueryWrapper<SysUserRolePO> urWrapper = new LambdaQueryWrapper<>();
        urWrapper.eq(SysUserRolePO::getRoleId, id);
        sysUserRoleDao.delete(urWrapper);
    }

    @Override
    public PageResult<SysRoleAggregate> findPage(RoleQuery query) {
        query.normalize();
        Page<SysRolePO> pageObj = new Page<>(query.getCurrent(), query.getSize());
        LambdaQueryWrapper<SysRolePO> wrapper = new LambdaQueryWrapper<>();
        if (query.getRoleName() != null && !query.getRoleName().isBlank()) {
            wrapper.like(SysRolePO::getRoleName, query.getRoleName());
        }
        if (query.getStatus() != null) {
            wrapper.eq(SysRolePO::getStatus, query.getStatus());
        }
        wrapper.orderByDesc(SysRolePO::getCreateTime);
        Page<SysRolePO> result = sysRoleDao.selectPage(pageObj, wrapper);
        List<SysRoleAggregate> records = result.getRecords().stream()
                .map(RoleInfraConvertor.INSTANCE::toDomain)
                .toList();
        return new PageResult<>(result.getCurrent(), result.getSize(),
                result.getTotal(), records);
    }

    @Override
    @Cached(name = "role:allEnabled", expire = 10, timeUnit = TimeUnit.MINUTES)
    public List<SysRoleAggregate> findAllEnabled() {
        LambdaQueryWrapper<SysRolePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRolePO::getStatus, 1);
        wrapper.orderByAsc(SysRolePO::getId);
        return sysRoleDao.selectList(wrapper).stream()
                .map(RoleInfraConvertor.INSTANCE::toDomain)
                .toList();
    }

    @Override
    @Cached(name = "role:byUser:", key = "#userId", expire = 5, timeUnit = TimeUnit.MINUTES)
    public List<SysRoleAggregate> findRolesByUserId(Long userId) {
        return sysUserRoleDao.selectRolesByUserId(userId).stream()
                .map(RoleInfraConvertor.INSTANCE::toDomain)
                .toList();
    }

    @Override
    public List<Long> findUserIdsByRoleId(Long roleId) {
        return sysUserRoleDao.selectUserIdsByRoleId(roleId);
    }

    @Override
    public void assignPermissions(Long roleId, List<Long> permissionIds) {
        LambdaQueryWrapper<SysRolePermissionPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRolePermissionPO::getRoleId, roleId);
        sysRolePermissionDao.delete(wrapper);
        if (permissionIds != null) {
            for (Long permissionId : permissionIds) {
                sysRolePermissionDao.insert(
                        RelationInfraConvertor.INSTANCE.toRolePermissionPo(SysRolePermission.of(roleId, permissionId)));
            }
        }
    }

    @Override
    public List<Long> findPermissionIdsByRoleId(Long roleId) {
        LambdaQueryWrapper<SysRolePermissionPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRolePermissionPO::getRoleId, roleId);
        return sysRolePermissionDao.selectList(wrapper).stream()
                .map(RelationInfraConvertor.INSTANCE::toRolePermissionDomain)
                .map(SysRolePermission::getPermissionId)
                .toList();
    }
}
