package com.silverwing.admin.application.command;

import com.silverwing.admin.application.convertor.UserConvertor;
import com.silverwing.biz.iam.domain.adapter.repository.UserRepository;
import com.silverwing.biz.iam.domain.model.aggregate.SysUserAggregate;
import com.silverwing.biz.iam.domain.service.IUserDomainService;
import com.silverwing.common.domain.ResultCode;
import com.silverwing.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户命令服务（CQRS 写侧）
 * <p>
 * 负责用户增删改、密码重置、状态切换、角色分配等会改变状态的用例。
 * 通过 biz-iam 的 Repository 端口访问领域数据，命令到实体的映射交由 UserConvertor。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserCommandService {

    private final UserRepository userRepository;
    private final UserConvertor userConvertor;
    private final IUserDomainService userDomainService;

    @Transactional
    public SysUserAggregate create(CreateUserCommand command) {
        SysUserAggregate user = userConvertor.toEntity(command);
        // 领域服务负责用户名唯一性校验与持久化
        user = userDomainService.registerUser(user);
        log.info("新建用户成功 username={}, id={}", user.getUsername(), user.getId());
        user.clearPassword();
        return user;
    }

    @Transactional
    public void update(Long id, UpdateUserCommand command) {
        SysUserAggregate user = userRepository.findById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }

        userConvertor.applyUpdate(user, command);
        // 领域服务负责持久化
        userDomainService.update(user);
        log.info("更新用户信息 id={}", id);
    }

    @Transactional
    public void delete(Long id) {
        // 领域服务负责删除（含级联清理）
        userDomainService.deleteById(id);
        log.info("删除用户 id={}", id);
    }

    @Transactional
    public void resetPassword(Long id, String newPassword) {
        SysUserAggregate user = userRepository.findById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }
        // 领域服务负责密码加密与持久化
        userDomainService.changePassword(user, newPassword);
        log.info("重置用户密码 id={}", id);
    }

    @Transactional
    public void toggleStatus(Long id) {
        SysUserAggregate user = userRepository.findById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }
        // 领域服务负责状态切换与持久化
        userDomainService.toggleStatus(user);
        log.info("切换用户状态 id={}, status={}", id, user.getStatus());
    }

    @Transactional
    public void assignRoles(Long userId, List<Long> roleIds) {
        // 领域服务负责角色全量分配
        userDomainService.assignRoles(userId, roleIds);
        log.info("分配用户角色 userId={}, roleIds={}", userId, roleIds);
    }
}
