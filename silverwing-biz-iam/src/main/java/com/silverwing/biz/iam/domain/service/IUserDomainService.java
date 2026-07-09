package com.silverwing.biz.iam.domain.service;

import com.silverwing.biz.iam.domain.model.aggregate.SysUserAggregate;

import java.util.List;

/**
 * 用户领域服务
 * <p>
 * 封装用户相关的领域规则与跨聚合编排（注册、密码变更、状态切换、角色分配）。
 * 依赖 {@code UserRepository} 端口访问数据，不感知具体持久化实现。
 * </p>
 */
public interface IUserDomainService {

    /**
     * 注册用户（含用户名唯一性校验）
     */
    SysUserAggregate registerUser(SysUserAggregate user);

    /**
     * 变更密码（加密后持久化）
     */
    void changePassword(SysUserAggregate user, String rawPassword);

    /**
     * 切换启用/禁用状态
     */
    void toggleStatus(SysUserAggregate user);

    /**
     * 为用户分配角色（全量覆盖）
     */
    void assignRoles(Long userId, List<Long> roleIds);

    /**
     * 更新用户信息（持久化）
     */
    SysUserAggregate update(SysUserAggregate user);

    /**
     * 删除用户（含级联清理）
     */
    void deleteById(Long id);
}
