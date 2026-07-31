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

    /** 按条件查询用户列表（非分页，用于导出） */
    List<SysUserAggregate> findList(UserQuery query);

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

    /**
     * 判断手机号是否被其他用户占用
     *
     * @param userId 当前用户 ID（排除自身）
     * @param phone  手机号
     * @return true 表示被占用
     */
    boolean existsByPhoneExcept(Long userId, String phone);

    /**
     * 判断邮箱是否被其他用户占用
     *
     * @param userId 当前用户 ID（排除自身）
     * @param email  邮箱
     * @return true 表示被占用
     */
    boolean existsByEmailExcept(Long userId, String email);
}
