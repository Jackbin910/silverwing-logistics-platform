package com.silverwing.admin.client.impl;

import com.silverwing.admin.application.command.CreateUserCommand;
import com.silverwing.admin.application.command.UpdateUserCommand;
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

    /**
     * 将本模块分页查询条件翻译为 biz-iam 领域查询对象
     */
    private UserQuery toUserQuery(UserPageQuery query) {
        UserQuery userQuery = new UserQuery();
        userQuery.setCurrent(query.getCurrent());
        userQuery.setSize(query.getSize());
        userQuery.setUsername(query.getUsername());
        userQuery.setStatus(query.getStatus());
        return userQuery;
    }
}
