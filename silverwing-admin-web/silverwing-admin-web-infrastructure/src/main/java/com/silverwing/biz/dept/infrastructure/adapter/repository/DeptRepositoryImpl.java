package com.silverwing.biz.dept.infrastructure.adapter.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.silverwing.biz.dept.domain.adapter.repository.DeptRepository;
import com.silverwing.biz.dept.domain.model.aggregate.SysDeptAggregate;
import com.silverwing.biz.dept.domain.model.query.DeptQuery;
import com.silverwing.biz.dept.infrastructure.adapter.repository.convertor.DeptInfraConvertor;
import com.silverwing.biz.dept.infrastructure.dao.SysDeptDao;
import com.silverwing.biz.dept.infrastructure.dao.SysRoleDeptDao;
import com.silverwing.biz.iam.infrastructure.dao.SysUserDao;
import com.silverwing.biz.iam.infrastructure.dao.po.SysUserPO;
import com.silverwing.biz.dept.infrastructure.dao.po.SysDeptPO;
import com.silverwing.biz.dept.infrastructure.dao.po.SysRoleDeptPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 部门仓储实现，基于 MyBatis-Plus 实现持久化与查询。
 */
@Repository
@RequiredArgsConstructor
public class DeptRepositoryImpl implements DeptRepository {

    private final SysDeptDao sysDeptDao;
    private final SysRoleDeptDao sysRoleDeptDao;
    private final SysUserDao sysUserDao;

    @Override
    public void save(SysDeptAggregate aggregate) {
        SysDeptPO po = DeptInfraConvertor.INSTANCE.toPo(aggregate);
        if (aggregate.getDeptId() == null) {
            po.setDeleted(0);
            sysDeptDao.insert(po);
            aggregate.setDeptId(po.getId());
        } else {
            sysDeptDao.updateById(po);
        }
    }

    @Override
    public void deleteById(Long deptId) {
        SysDeptPO po = new SysDeptPO();
        po.setId(deptId);
        po.setDeleted(1);
        sysDeptDao.updateById(po);
    }

    @Override
    public SysDeptAggregate findById(Long deptId) {
        SysDeptPO po = sysDeptDao.selectById(deptId);
        return po == null ? null : DeptInfraConvertor.INSTANCE.toDomain(po);
    }

    @Override
    public List<SysDeptAggregate> findList(DeptQuery query) {
        LambdaQueryWrapper<SysDeptPO> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(query.getStatus() != null, SysDeptPO::getStatus, query.getStatus())
                .like(query.getDeptName() != null, SysDeptPO::getDeptName, query.getDeptName())
                .orderByAsc(SysDeptPO::getParentId)
                .orderByAsc(SysDeptPO::getOrderNum);
        return toAggregates(sysDeptDao.selectList(wrapper));
    }

    @Override
    public List<SysDeptAggregate> findAll() {
        LambdaQueryWrapper<SysDeptPO> wrapper = Wrappers.lambdaQuery();
        wrapper.orderByAsc(SysDeptPO::getParentId).orderByAsc(SysDeptPO::getOrderNum);
        return toAggregates(sysDeptDao.selectList(wrapper));
    }

    @Override
    public long countByParentId(Long parentId) {
        LambdaQueryWrapper<SysDeptPO> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(SysDeptPO::getParentId, parentId);
        return sysDeptDao.selectCount(wrapper);
    }

    @Override
    public long countNormalChildren(Long deptId) {
        LambdaQueryWrapper<SysDeptPO> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(SysDeptPO::getParentId, deptId).eq(SysDeptPO::getStatus, "0");
        return sysDeptDao.selectCount(wrapper);
    }

    @Override
    public long countUserByDeptId(Long deptId) {
        LambdaQueryWrapper<SysUserPO> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(SysUserPO::getDeptId, deptId);
        return sysUserDao.selectCount(wrapper);
    }

    @Override
    public List<SysDeptAggregate> findChildren(Long deptId) {
        LambdaQueryWrapper<SysDeptPO> wrapper = Wrappers.lambdaQuery();
        wrapper.apply("find_in_set({0}, ancestors)", deptId);
        return toAggregates(sysDeptDao.selectList(wrapper));
    }

    @Override
    public SysDeptAggregate findByParentIdAndName(Long parentId, String deptName, Long excludeDeptId) {
        LambdaQueryWrapper<SysDeptPO> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(SysDeptPO::getParentId, parentId)
                .eq(SysDeptPO::getDeptName, deptName)
                .ne(excludeDeptId != null, SysDeptPO::getId, excludeDeptId);
        SysDeptPO po = sysDeptDao.selectOne(wrapper);
        return po == null ? null : DeptInfraConvertor.INSTANCE.toDomain(po);
    }

    @Override
    public void updateChildren(List<SysDeptAggregate> children) {
        for (SysDeptAggregate child : children) {
            sysDeptDao.updateById(DeptInfraConvertor.INSTANCE.toPo(child));
        }
    }

    @Override
    public List<Long> findRoleDeptIds(Long roleId) {
        LambdaQueryWrapper<SysRoleDeptPO> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(SysRoleDeptPO::getRoleId, roleId);
        return sysRoleDeptDao.selectList(wrapper).stream()
                .map(SysRoleDeptPO::getDeptId)
                .collect(Collectors.toList());
    }

    private List<SysDeptAggregate> toAggregates(List<SysDeptPO> pos) {
        return pos.stream().map(DeptInfraConvertor.INSTANCE::toDomain).collect(Collectors.toList());
    }
}
