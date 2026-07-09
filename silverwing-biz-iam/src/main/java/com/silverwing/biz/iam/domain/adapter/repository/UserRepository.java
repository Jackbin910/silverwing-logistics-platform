package com.silverwing.biz.iam.domain.adapter.repository;

import com.silverwing.common.domain.PageResult;
import com.silverwing.biz.iam.domain.model.aggregate.SysUserAggregate;
import com.silverwing.biz.iam.domain.model.query.UserQuery;

import java.util.List;

/**
 * 用户仓储接口（领域契约/端口）
 * <p>
 * 定义用户聚合根的数据访问契约，具体实现在 infrastructure/adapter/repository。
 * 对齐 kaleido 的 domain/adapter/repository 端口位置（依赖倒置）。
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
}
