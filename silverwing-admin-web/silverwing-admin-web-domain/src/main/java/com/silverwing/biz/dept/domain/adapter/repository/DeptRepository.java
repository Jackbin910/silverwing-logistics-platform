package com.silverwing.biz.dept.domain.adapter.repository;

import com.silverwing.biz.dept.domain.model.aggregate.SysDeptAggregate;
import com.silverwing.biz.dept.domain.model.query.DeptQuery;

import java.util.List;

/**
 * 部门仓储接口，定义部门聚合根的持久化与查询能力。
 */
public interface DeptRepository {

    /** 保存部门（新增或更新） */
    void save(SysDeptAggregate aggregate);

    /** 逻辑删除部门 */
    void deleteById(Long deptId);

    /** 根据主键查询部门 */
    SysDeptAggregate findById(Long deptId);

    /** 条件查询部门列表 */
    List<SysDeptAggregate> findList(DeptQuery query);

    /** 查询全部部门 */
    List<SysDeptAggregate> findAll();

    /** 统计子部门数量 */
    long countByParentId(Long parentId);

    /** 统计未停用的子部门数量 */
    long countNormalChildren(Long deptId);

    /** 统计部门下未删除的用户数量 */
    long countUserByDeptId(Long deptId);

    /** 查询部门下所有后代部门（ancestors 包含 deptId） */
    List<SysDeptAggregate> findChildren(Long deptId);

    /** 根据父部门ID与部门名称查询（用于唯一性校验，excludeDeptId 为自身ID可传 null） */
    SysDeptAggregate findByParentIdAndName(Long parentId, String deptName, Long excludeDeptId);

    /** 批量更新部门（用于同步祖先列表） */
    void updateChildren(List<SysDeptAggregate> children);

    /** 根据角色ID查询关联的部门ID列表 */
    List<Long> findRoleDeptIds(Long roleId);
}
