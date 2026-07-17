package com.silverwing.admin.client;

import com.silverwing.admin.application.command.CreateUserCommand;
import com.silverwing.admin.application.command.UpdateUserCommand;
import com.silverwing.admin.application.dto.UserResponse;
import com.silverwing.admin.application.query.UserPageQuery;
import com.silverwing.common.domain.PageResult;

import java.util.List;

/**
 * IAM 用户上下文防腐层端口
 * <p>
 * admin-web 应用层通过该端口访问 biz-iam 用户上下文，完全不直接依赖其领域聚合根、
 * 仓储或领域服务。端口的具体实现（适配器）位于 {@code client.impl} 包，
 * biz-iam 的领域对象仅在适配器内部出现，从而隔离两个限界上下文的耦合。
 * </p>
 */
public interface IamUserClient {

    /**
     * 创建用户
     */
    UserResponse create(CreateUserCommand command);

    /**
     * 更新用户信息
     */
    void update(Long id, UpdateUserCommand command);

    /**
     * 删除用户
     */
    void delete(Long id);

    /**
     * 重置用户密码
     */
    void resetPassword(Long id, String newPassword);

    /**
     * 切换用户启用/禁用状态
     */
    void toggleStatus(Long id);

    /**
     * 为用户分配角色（全量覆盖）
     */
    void assignRoles(Long userId, List<Long> roleIds);

    /**
     * 分页查询用户列表
     */
    PageResult<UserResponse> list(UserPageQuery query);

    /**
     * 根据ID查询用户
     */
    UserResponse getById(Long id);

    /**
     * 查询用户已分配的角色ID列表
     */
    List<Long> getUserRoleIds(Long userId);
}
