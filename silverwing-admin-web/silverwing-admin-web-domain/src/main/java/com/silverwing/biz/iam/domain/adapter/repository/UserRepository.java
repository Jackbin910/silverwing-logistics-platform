package com.silverwing.biz.iam.domain.adapter.repository;

import com.silverwing.common.domain.PageResult;
import com.silverwing.biz.iam.domain.model.aggregate.SysUserAggregate;
import com.silverwing.biz.iam.domain.model.query.UserQuery;

import java.util.List;

/**
 * 用户仓储接口（领域契约/端口）
 * <p>
 * 定义用户聚合根的数据访问契约，具体实现在 infrastructure/adapter/repository。
 * </p>
 */
public interface UserRepository {

    SysUserAggregate findById(Long id);

    SysUserAggregate findByUsername(String username);

    boolean existsByUsername(String username);

    void save(SysUserAggregate user);

    void deleteById(Long id);

    PageResult<SysUserAggregate> findPage(UserQuery query);

    List<Long> findRoleIdsByUserId(Long userId);

    void assignRoles(Long userId, List<Long> roleIds);

    void deleteUserRoles(Long userId);

    /** 查询指定角色下的用户（分页） */
    PageResult<SysUserAggregate> findPageByRoleId(UserQuery query, Long roleId);

    /** 查询未分配指定角色的用户（分页） */
    PageResult<SysUserAggregate> findPageWithoutRole(UserQuery query, Long roleId);

    /** 为用户授予角色（幂等） */
    void assignRoleToUser(Long userId, Long roleId);

    /** 移除用户的某个角色 */
    void removeRoleFromUser(Long userId, Long roleId);
}
