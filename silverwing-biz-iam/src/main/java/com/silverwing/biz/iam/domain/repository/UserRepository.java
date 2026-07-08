package com.silverwing.biz.iam.domain.repository;

import com.silverwing.common.domain.PageResult;
import com.silverwing.biz.iam.domain.model.SysUser;
import com.silverwing.biz.iam.domain.model.UserQuery;

import java.util.List;

/**
 * 用户仓储接口（领域契约）
 * <p>
 * 定义用户聚合根的数据访问契约，具体的 MyBatis-Plus 实现在 infrastructure 层。
 * </p>
 */
public interface UserRepository {

    SysUser findById(Long id);

    SysUser findByUsername(String username);

    boolean existsByUsername(String username);

    void save(SysUser user);

    void deleteById(Long id);

    PageResult<SysUser> findPage(UserQuery query);

    List<Long> findRoleIdsByUserId(Long userId);

    void assignRoles(Long userId, List<Long> roleIds);

    void deleteUserRoles(Long userId);
}
