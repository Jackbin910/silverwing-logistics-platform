package com.silverwing.auth.iam.domain.adapter.repository;

import com.silverwing.auth.iam.domain.model.aggregate.AuthUserAggregate;

/**
 * 用户仓储端口（领域契约）
 * <p>定义认证所需的数据访问契约，具体实现位于 auth-infrastructure。</p>
 */
public interface UserRepository {

    /**
     * 根据用户名查询用户（用于登录）
     *
     * @param username 用户名
     * @return 用户聚合根，不存在时返回 null
     */
    AuthUserAggregate findByUsername(String username);

    /**
     * 根据用户ID查询用户
     *
     * @param id 用户ID
     * @return 用户聚合根，不存在时返回 null
     */
    AuthUserAggregate findById(Long id);
}
