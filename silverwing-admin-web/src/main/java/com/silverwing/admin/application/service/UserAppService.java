package com.silverwing.admin.application.service;

import cn.hutool.crypto.digest.BCrypt;
import com.silverwing.admin.application.command.CreateUserCommand;
import com.silverwing.admin.application.command.UpdateUserCommand;
import com.silverwing.common.domain.PageResult;
import com.silverwing.common.domain.ResultCode;
import com.silverwing.common.domain.model.SysUser;
import com.silverwing.common.domain.model.UserQuery;
import com.silverwing.common.domain.repository.UserRepository;
import com.silverwing.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户管理应用服务
 * <p>
 * 编排用户增删改查用例，通过 Repository 访问数据，
 * 使用 SysUser 聚合根的领域行为（enable/disable/changePassword）。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserAppService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public PageResult<SysUser> list(UserQuery query) {
        return userRepository.findPage(query);
    }

    @Transactional(readOnly = true)
    public SysUser getById(Long id) {
        SysUser user = userRepository.findById(id);
        if (user != null) {
            user.clearPassword();
        }
        return user;
    }

    @Transactional
    public SysUser create(CreateUserCommand command) {
        if (userRepository.existsByUsername(command.getUsername())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "用户名已存在");
        }

        SysUser user = new SysUser();
        user.setUsername(command.getUsername());
        user.changePassword(BCrypt.hashpw(command.getPassword(), BCrypt.gensalt()));
        user.setSex(command.getSex());
        user.setAvatar(command.getAvatar());
        user.setPhone(command.getPhone());
        user.setEmail(command.getEmail());
        user.enable(); // 领域行为：默认启用

        userRepository.save(user);
        log.info("新建用户成功 username={}, id={}", user.getUsername(), user.getId());
        user.clearPassword();
        return user;
    }

    @Transactional
    public void update(Long id, UpdateUserCommand command) {
        SysUser user = userRepository.findById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }

        if (command.getAvatar() != null) user.setAvatar(command.getAvatar());
        if (command.getPhone() != null) user.setPhone(command.getPhone());
        if (command.getEmail() != null) user.setEmail(command.getEmail());
        if (command.getSex() != null) user.setSex(command.getSex());
        if (command.getStatus() != null) {
            if (command.getStatus() == 1) user.enable();
            else user.disable();
        }

        userRepository.save(user);
        log.info("更新用户信息 id={}", id);
    }

    @Transactional
    public void delete(Long id) {
        userRepository.deleteById(id);
        log.info("删除用户 id={}", id);
    }

    @Transactional
    public void resetPassword(Long id, String newPassword) {
        SysUser user = userRepository.findById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }
        user.changePassword(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
        userRepository.save(user);
        log.info("重置用户密码 id={}", id);
    }

    @Transactional
    public void toggleStatus(Long id) {
        SysUser user = userRepository.findById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }
        user.toggleStatus(); // 领域行为
        userRepository.save(user);
        log.info("切换用户状态 id={}, status={}", id, user.getStatus());
    }

    @Transactional
    public void assignRoles(Long userId, List<Long> roleIds) {
        userRepository.assignRoles(userId, roleIds);
        log.info("分配用户角色 userId={}, roleIds={}", userId, roleIds);
    }

    @Transactional(readOnly = true)
    public List<Long> getUserRoleIds(Long userId) {
        return userRepository.findRoleIdsByUserId(userId);
    }
}
