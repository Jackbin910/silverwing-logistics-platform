package com.silverwing.admin.client.impl;

import com.silverwing.admin.application.command.CreateUserCommand;
import com.silverwing.admin.application.command.UpdateProfileCommand;
import com.silverwing.admin.application.command.UpdateProfilePasswordCommand;
import com.silverwing.admin.application.command.UpdateUserCommand;
import com.silverwing.admin.application.command.UserImportCommand;
import com.silverwing.admin.application.dto.UserResponse;
import com.silverwing.admin.application.query.UserPageQuery;
import com.silverwing.admin.client.IamUserClient;
import com.silverwing.admin.client.convertor.UserConvertor;
import com.silverwing.biz.iam.domain.adapter.repository.UserRepository;
import com.silverwing.biz.iam.domain.model.aggregate.SysUserAggregate;
import com.silverwing.biz.iam.domain.model.query.UserQuery;
import com.silverwing.biz.iam.domain.service.IUserDomainService;
import com.silverwing.common.domain.PageResult;
import com.silverwing.common.domain.ResultCode;
import com.silverwing.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * IAM 用户上下文防腐层适配器
 * <p>
 * 本类是唯一直接依赖 biz-iam 领域层（聚合根、仓储、领域服务）的地方。
 * 负责将 admin-web 的命令/查询翻译为 biz-iam 的领域对象，并回写为本模块响应 DTO。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IamUserClientImpl implements IamUserClient {

    private final UserRepository userRepository;
    private final UserConvertor userConvertor;
    private final IUserDomainService userDomainService;

    @Override
    @Transactional
    public UserResponse create(CreateUserCommand command) {
        SysUserAggregate user = userConvertor.toEntity(command);
        // 领域服务负责用户名唯一性校验与持久化
        user = userDomainService.registerUser(user);
        log.info("新建用户成功 username={}, id={}", user.getUsername(), user.getId());
        return userConvertor.toResponse(user);
    }

    @Override
    @Transactional
    public void update(Long id, UpdateUserCommand command) {
        SysUserAggregate user = userRepository.findById(id);
        if (user == null) {
            throw BusinessException.i18n(ResultCode.NOT_FOUND, "admin.user.notfound");
        }
        userConvertor.applyUpdate(user, command);
        // 领域服务负责持久化
        userDomainService.update(user);
        log.info("更新用户信息 id={}", id);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        // 领域服务负责删除（含级联清理）
        userDomainService.deleteById(id);
        log.info("删除用户 id={}", id);
    }

    @Override
    @Transactional
    public void resetPassword(Long id, String newPassword) {
        SysUserAggregate user = userRepository.findById(id);
        if (user == null) {
            throw BusinessException.i18n(ResultCode.NOT_FOUND, "admin.user.notfound");
        }
        // 领域服务负责密码加密与持久化
        userDomainService.changePassword(user, newPassword);
        log.info("重置用户密码 id={}", id);
    }

    @Override
    @Transactional
    public void toggleStatus(Long id) {
        SysUserAggregate user = userRepository.findById(id);
        if (user == null) {
            throw BusinessException.i18n(ResultCode.NOT_FOUND, "admin.user.notfound");
        }
        // 领域服务负责状态切换与持久化
        userDomainService.toggleStatus(user);
        log.info("切换用户状态 id={}, status={}", id, user.getStatus());
    }

    @Override
    @Transactional
    public void assignRoles(Long userId, List<Long> roleIds) {
        // 领域服务负责角色全量分配
        userDomainService.assignRoles(userId, roleIds);
        log.info("分配用户角色 userId={}, roleIds={}", userId, roleIds);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<UserResponse> list(UserPageQuery query) {
        UserQuery userQuery = toUserQuery(query);
        PageResult<SysUserAggregate> page = userRepository.findPage(userQuery);
        List<UserResponse> records = page.getRecords().stream()
                .map(userConvertor::toResponse)
                .collect(Collectors.toList());
        return new PageResult<>(page.getCurrent(), page.getSize(), page.getTotal(), records);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        SysUserAggregate user = userRepository.findById(id);
        return user == null ? null : userConvertor.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getUserRoleIds(Long userId) {
        return userRepository.findRoleIdsByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<UserResponse> listAllocatedToRole(Long roleId, UserPageQuery query) {
        UserQuery userQuery = toUserQuery(query);
        PageResult<SysUserAggregate> page = userRepository.findPageByRoleId(userQuery, roleId);
        return toUserPage(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<UserResponse> listUnallocatedToRole(Long roleId, UserPageQuery query) {
        UserQuery userQuery = toUserQuery(query);
        PageResult<SysUserAggregate> page = userRepository.findPageWithoutRole(userQuery, roleId);
        return toUserPage(page);
    }

    @Override
    @Transactional
    public void removeRoleFromUser(Long roleId, Long userId) {
        userDomainService.removeRoleFromUser(userId, roleId);
        log.info("取消用户角色 roleId={}, userId={}", roleId, userId);
    }

    @Override
    @Transactional
    public void removeRolesFromUser(Long roleId, List<Long> userIds) {
        if (userIds != null) {
            for (Long userId : userIds) {
                userDomainService.removeRoleFromUser(userId, roleId);
            }
        }
        log.info("批量取消用户角色 roleId={}, 数量={}", roleId, userIds == null ? 0 : userIds.size());
    }

    @Override
    @Transactional
    public void addRoleToUsers(Long roleId, List<Long> userIds) {
        if (userIds != null) {
            for (Long userId : userIds) {
                userDomainService.assignRoleToUser(userId, roleId);
            }
        }
        log.info("批量授予用户角色 roleId={}, 数量={}", roleId, userIds == null ? 0 : userIds.size());
    }

    /**
     * 将本模块分页查询条件翻译为 biz-iam 领域查询对象
     */
    private UserQuery toUserQuery(UserPageQuery query) {
        UserQuery userQuery = new UserQuery();
        userQuery.setCurrent(query.getCurrent());
        userQuery.setSize(query.getSize());
        userQuery.setUsername(query.getUsername());
        userQuery.setPhonenumber(query.getPhonenumber());
        userQuery.setStatus(query.getStatus());
        return userQuery;
    }

    /**
     * 将用户领域聚合根分页结果转换为本模块响应 DTO 分页结果
     */
    private PageResult<UserResponse> toUserPage(PageResult<SysUserAggregate> page) {
        List<UserResponse> records = page.getRecords().stream()
                .map(userConvertor::toResponse)
                .collect(Collectors.toList());
        return new PageResult<>(page.getCurrent(), page.getSize(), page.getTotal(), records);
    }

    @Override
    public List<UserResponse> exportList(UserPageQuery query) {
        UserQuery userQuery = toUserQuery(query);
        return userRepository.findList(userQuery).stream()
                .map(userConvertor::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public int importUsers(List<UserImportCommand> rows, boolean updateSupport) {
        int success = 0;
        for (UserImportCommand row : rows) {
            if (row.getUsername() == null || row.getUsername().isBlank()) {
                continue;
            }
            SysUserAggregate exist = userRepository.findByUsername(row.getUsername());
            if (exist != null) {
                if (!updateSupport) {
                    continue;
                }
                UpdateUserCommand update = new UpdateUserCommand();
                update.setNickname(row.getNickname());
                update.setPhone(row.getPhone());
                update.setEmail(row.getEmail());
                update.setSex(row.getSex());
                update.setStatus(row.getStatus());
                update.setDeptId(row.getDeptId());
                update.setUserType(row.getUserType());
                this.update(exist.getId(), update);
            } else {
                CreateUserCommand create = new CreateUserCommand();
                create.setUsername(row.getUsername());
                create.setNickname(row.getNickname());
                create.setPhone(row.getPhone());
                create.setEmail(row.getEmail());
                create.setSex(row.getSex());
                create.setDeptId(row.getDeptId());
                create.setUserType(row.getUserType());
                create.setPassword(DEFAULT_INIT_PASSWORD);
                this.create(create);
            }
            success++;
        }
        log.info("导入用户完成，成功条数={}, 覆盖更新={}", success, updateSupport);
        return success;
    }

    /** 导入用户默认初始密码 */
    private static final String DEFAULT_INIT_PASSWORD = "123456";

    @Override
    @Transactional
    public void updateProfile(Long userId, UpdateProfileCommand command) {
        SysUserAggregate user = userRepository.findById(userId);
        if (user == null) {
            throw BusinessException.i18n(ResultCode.NOT_FOUND, "admin.user.notfound");
        }
        if (command.getNickname() != null) {
            user.setNickname(command.getNickname());
        }
        if (command.getEmail() != null) {
            user.setEmail(command.getEmail());
        }
        if (command.getPhone() != null) {
            user.setPhone(command.getPhone());
        }
        if (command.getSex() != null) {
            user.setSex(command.getSex());
        }
        // 领域服务负责手机号/邮箱唯一性校验与持久化
        userDomainService.updateProfile(user);
        log.info("更新个人资料 userId={}", userId);
    }

    @Override
    @Transactional
    public void updateSelfPassword(Long userId, String oldPassword, String newPassword) {
        SysUserAggregate user = userRepository.findById(userId);
        if (user == null) {
            throw BusinessException.i18n(ResultCode.NOT_FOUND, "admin.user.notfound");
        }
        // 领域服务负责旧密码校验、新密码差异化校验与加密持久化
        userDomainService.updateSelfPassword(user, oldPassword, newPassword);
        log.info("修改密码 userId={}", userId);
    }

    @Override
    @Transactional
    public void updateAvatar(Long userId, String avatarUrl) {
        SysUserAggregate user = userRepository.findById(userId);
        if (user == null) {
            throw BusinessException.i18n(ResultCode.NOT_FOUND, "admin.user.notfound");
        }
        // 领域服务负责持久化头像地址
        userDomainService.updateAvatar(user, avatarUrl);
        log.info("更新头像 userId={}", userId);
    }
}
