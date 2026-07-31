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
     * 为用户授予单个角色（幂等）
     */
    void assignRoleToUser(Long userId, Long roleId);

    /**
     * 移除用户的单个角色
     */
    void removeRoleFromUser(Long userId, Long roleId);

    /**
     * 更新用户信息（持久化）
     */
    SysUserAggregate update(SysUserAggregate user);

    /**
     * 删除用户（含级联清理）
     */
    void deleteById(Long id);

    /**
     * 修改个人资料（昵称、邮箱、手机号、性别），并对手机号/邮箱做唯一性校验
     *
     * @param user 待更新的用户聚合根（含最新资料字段）
     */
    void updateProfile(SysUserAggregate user);

    /**
     * 修改本人密码（校验旧密码且新密码不得与旧密码相同）
     *
     * @param user        当前用户聚合根
     * @param oldPassword 旧密码（明文）
     * @param newPassword 新密码（明文）
     */
    void updateSelfPassword(SysUserAggregate user, String oldPassword, String newPassword);

    /**
     * 更新头像地址
     *
     * @param user      当前用户聚合根
     * @param avatarUrl 头像访问地址
     */
    void updateAvatar(SysUserAggregate user, String avatarUrl);
}
