package com.silverwing.biz.dept.domain.service;

import com.silverwing.biz.dept.domain.model.aggregate.SysDeptAggregate;
import com.silverwing.biz.dept.domain.model.query.DeptQuery;

import java.util.List;

/**
 * 部门领域服务，封装部门核心业务规则。
 */
public interface IDeptDomainService {

    /** 新增部门 */
    void saveDept(SysDeptAggregate aggregate);

    /** 修改部门 */
    void updateDept(SysDeptAggregate aggregate);

    /** 删除部门（校验子部门与用户） */
    void deleteDeptById(Long deptId);

    /** 保存部门排序 */
    void saveDeptSort(Long deptId, Integer orderNum);

    /** 根据主键查询 */
    SysDeptAggregate findById(Long deptId);

    /** 条件查询 */
    List<SysDeptAggregate> findList(DeptQuery query);

    /** 查询全部 */
    List<SysDeptAggregate> findAll();

    /** 查询后代部门 */
    List<SysDeptAggregate> findChildren(Long deptId);

    /** 根据角色ID查询关联部门ID */
    List<Long> findRoleDeptIds(Long roleId);

    /** 校验部门名称是否唯一（重复则抛异常） */
    void checkDeptNameUnique(SysDeptAggregate aggregate);
}
