package com.silverwing.admin.client;

import com.silverwing.admin.application.command.CreateUserCommand;
import com.silverwing.admin.application.command.UpdateProfileCommand;
import com.silverwing.admin.application.command.UpdateProfilePasswordCommand;
import com.silverwing.admin.application.command.UpdateUserCommand;
import com.silverwing.admin.application.command.UserImportCommand;
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

    /**
     * 查询角色已分配的用户列表（分页）
     */
    PageResult<UserResponse> listAllocatedToRole(Long roleId, UserPageQuery query);

    /**
     * 查询角色未分配的用户列表（分页）
     */
    PageResult<UserResponse> listUnallocatedToRole(Long roleId, UserPageQuery query);

    /**
     * 取消角色与单个用户的授权
     */
    void removeRoleFromUser(Long roleId, Long userId);

    /**
     * 批量取消角色与用户的授权
     */
    void removeRolesFromUser(Long roleId, List<Long> userIds);

    /**
     * 批量为角色授予用户
     */
    void addRoleToUsers(Long roleId, List<Long> userIds);

    /**
     * 导出查询：按条件返回用户列表（非分页）
     */
    List<UserResponse> exportList(UserPageQuery query);

    /**
     * 导入用户（Excel 解析结果）
     *
     * @param rows          解析后的用户行
     * @param updateSupport 已存在用户是否覆盖更新
     * @return 成功导入/更新条数
     */
    int importUsers(List<UserImportCommand> rows, boolean updateSupport);

    /**
     * 修改个人资料（昵称、邮箱、手机号、性别），含手机号/邮箱唯一性校验
     *
     * @param userId  用户 ID
     * @param command 资料修改命令
     */
    void updateProfile(Long userId, UpdateProfileCommand command);

    /**
     * 修改密码（校验旧密码且新密码不得与旧密码相同）
     *
     * @param userId       用户 ID
     * @param oldPassword  旧密码
     * @param newPassword  新密码
     */
    void updateSelfPassword(Long userId, String oldPassword, String newPassword);

    /**
     * 更新头像地址
     *
     * @param userId    用户 ID
     * @param avatarUrl 头像访问地址
     */
    void updateAvatar(Long userId, String avatarUrl);
}
