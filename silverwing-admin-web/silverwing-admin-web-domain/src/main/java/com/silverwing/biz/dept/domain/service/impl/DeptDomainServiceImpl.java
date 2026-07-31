package com.silverwing.biz.dept.domain.service.impl;

import com.silverwing.biz.dept.domain.adapter.repository.DeptRepository;
import com.silverwing.biz.dept.domain.model.aggregate.SysDeptAggregate;
import com.silverwing.biz.dept.domain.model.query.DeptQuery;
import com.silverwing.biz.dept.domain.service.IDeptDomainService;
import com.silverwing.common.domain.ResultCode;
import com.silverwing.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 部门领域服务实现。
 * <p>
 * 负责部门新增/修改时的祖级列表计算、同级部门名称唯一性校验、
 * 部门删除时的子部门与用户存在性校验等业务规则。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeptDomainServiceImpl implements IDeptDomainService {

    /** 部门正常状态 */
    public static final String DEPT_NORMAL = "0";
    /** 部门停用状态 */
    public static final String DEPT_DISABLE = "1";
    /** 顶级节点祖级标识 */
    private static final String ROOT_ANCESTORS = "0";

    private final DeptRepository deptRepository;

    @Override
    public void saveDept(SysDeptAggregate aggregate) {
        buildAncestors(aggregate);
        checkDeptNameUnique(aggregate);
        deptRepository.save(aggregate);
        log.info("新增部门 deptId={}, deptName={}", aggregate.getDeptId(), aggregate.getDeptName());
    }

    @Override
    public void updateDept(SysDeptAggregate aggregate) {
        SysDeptAggregate oldDept = deptRepository.findById(aggregate.getDeptId());
        if (oldDept == null) {
            throw BusinessException.i18n(ResultCode.NOT_FOUND, "admin.dept.notfound");
        }
        // 上级部门不能是自己
        if (aggregate.getDeptId().equals(aggregate.getParentId())) {
            throw BusinessException.i18n(ResultCode.DATA_STATUS_ILLEGAL, "admin.dept.parent.self");
        }
        // 父部门变化则重建祖级列表并同步子部门
        if (!equalsParent(oldDept.getParentId(), aggregate.getParentId())) {
            buildAncestors(aggregate);
            deptRepository.save(aggregate);
            updateDeptChildren(aggregate.getDeptId(), aggregate.getAncestors());
        } else {
            aggregate.setAncestors(oldDept.getAncestors());
            deptRepository.save(aggregate);
        }
        // 部门停用但存在未停用子部门，禁止
        if (DEPT_DISABLE.equals(aggregate.getStatus())
                && deptRepository.countNormalChildren(aggregate.getDeptId()) > 0) {
            throw BusinessException.i18n(ResultCode.DATA_STATUS_ILLEGAL, "admin.dept.child.normal.exists");
        }
        checkDeptNameUnique(aggregate);
    }

    @Override
    public void deleteDeptById(Long deptId) {
        if (deptRepository.countByParentId(deptId) > 0) {
            throw BusinessException.i18n(ResultCode.DATA_STATUS_ILLEGAL, "admin.dept.has.child");
        }
        if (deptRepository.countUserByDeptId(deptId) > 0) {
            throw BusinessException.i18n(ResultCode.DATA_STATUS_ILLEGAL, "admin.dept.has.user");
        }
        deptRepository.deleteById(deptId);
        log.info("删除部门 deptId={}", deptId);
    }

    @Override
    public void saveDeptSort(Long deptId, Integer orderNum) {
        SysDeptAggregate aggregate = deptRepository.findById(deptId);
        if (aggregate == null) {
            throw BusinessException.i18n(ResultCode.NOT_FOUND, "admin.dept.notfound");
        }
        aggregate.setOrderNum(orderNum);
        deptRepository.save(aggregate);
    }

    @Override
    public SysDeptAggregate findById(Long deptId) {
        return deptRepository.findById(deptId);
    }

    @Override
    public List<SysDeptAggregate> findList(DeptQuery query) {
        return deptRepository.findList(query);
    }

    @Override
    public List<SysDeptAggregate> findAll() {
        return deptRepository.findAll();
    }

    @Override
    public List<SysDeptAggregate> findChildren(Long deptId) {
        return deptRepository.findChildren(deptId);
    }

    @Override
    public List<Long> findRoleDeptIds(Long roleId) {
        return deptRepository.findRoleDeptIds(roleId);
    }

    @Override
    public void checkDeptNameUnique(SysDeptAggregate aggregate) {
        Long selfId = aggregate.getDeptId();
        SysDeptAggregate exist = deptRepository.findByParentIdAndName(
                aggregate.getParentId(), aggregate.getDeptName(), selfId);
        if (exist != null) {
            throw BusinessException.i18n(ResultCode.DATA_ALREADY_EXISTS,
                    "admin.dept.name.exists", aggregate.getDeptName());
        }
    }

    /** 构建祖级列表 */
    private void buildAncestors(SysDeptAggregate aggregate) {
        Long parentId = aggregate.getParentId();
        if (parentId == null || parentId == 0L) {
            aggregate.setParentId(parentId == null ? 0L : parentId);
            aggregate.setAncestors(ROOT_ANCESTORS);
            return;
        }
        SysDeptAggregate parent = deptRepository.findById(parentId);
        if (parent == null) {
            aggregate.setAncestors(ROOT_ANCESTORS);
            return;
        }
        if (!DEPT_NORMAL.equals(parent.getStatus())) {
            throw BusinessException.i18n(ResultCode.DATA_STATUS_ILLEGAL, "admin.dept.parent.disabled");
        }
        aggregate.setAncestors(parent.getAncestors() + "," + parent.getDeptId());
    }

    /** 递归更新后代部门祖级列表 */
    private void updateDeptChildren(Long parentId, String parentAncestors) {
        List<SysDeptAggregate> children = deptRepository.findChildren(parentId);
        for (SysDeptAggregate child : children) {
            String newAncestors = parentAncestors + "," + parentId;
            child.setAncestors(newAncestors);
            deptRepository.save(child);
            updateDeptChildren(child.getDeptId(), newAncestors);
        }
    }

    private boolean equalsParent(Long oldParentId, Long newParentId) {
        long a = oldParentId == null ? 0L : oldParentId;
        long b = newParentId == null ? 0L : newParentId;
        return a == b;
    }
}
