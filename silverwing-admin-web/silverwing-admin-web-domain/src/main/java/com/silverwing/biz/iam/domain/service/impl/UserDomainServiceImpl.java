package com.silverwing.biz.iam.domain.service.impl;

import com.silverwing.biz.iam.domain.adapter.repository.UserRepository;
import com.silverwing.biz.iam.domain.model.aggregate.SysUserAggregate;
import com.silverwing.biz.iam.domain.service.IUserDomainService;
import cn.hutool.core.text.CharSequenceUtil;
import com.silverwing.common.domain.ResultCode;
import com.silverwing.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户领域服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDomainServiceImpl implements IUserDomainService {

    private final UserRepository userRepository;

    @Override
    public SysUserAggregate registerUser(SysUserAggregate user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw BusinessException.i18n(ResultCode.DATA_ALREADY_EXISTS, "validation.user.username.exists");
        }
        userRepository.save(user);
        return user;
    }

    @Override
    public void changePassword(SysUserAggregate user, String rawPassword) {
        user.changePassword(rawPassword);
        userRepository.save(user);
    }

    @Override
    public void toggleStatus(SysUserAggregate user) {
        user.toggleStatus();
        userRepository.save(user);
    }

    @Override
    public void assignRoles(Long userId, List<Long> roleIds) {
        userRepository.assignRoles(userId, roleIds);
    }

    @Override
    public void assignRoleToUser(Long userId, Long roleId) {
        userRepository.assignRoleToUser(userId, roleId);
    }

    @Override
    public void removeRoleFromUser(Long userId, Long roleId) {
        userRepository.removeRoleFromUser(userId, roleId);
    }

    @Override
    public SysUserAggregate update(SysUserAggregate user) {
        userRepository.save(user);
        return user;
    }

    @Override
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public void updateProfile(SysUserAggregate user) {
        if (CharSequenceUtil.isNotBlank(user.getPhone())
                && userRepository.existsByPhoneExcept(user.getId(), user.getPhone())) {
            throw BusinessException.i18n(ResultCode.DATA_ALREADY_EXISTS, "validation.user.phone.exists");
        }
        if (CharSequenceUtil.isNotBlank(user.getEmail())
                && userRepository.existsByEmailExcept(user.getId(), user.getEmail())) {
            throw BusinessException.i18n(ResultCode.DATA_ALREADY_EXISTS, "validation.user.email.exists");
        }
        userRepository.save(user);
    }

    @Override
    public void updateSelfPassword(SysUserAggregate user, String oldPassword, String newPassword) {
        if (CharSequenceUtil.isBlank(oldPassword) || CharSequenceUtil.isBlank(newPassword)) {
            throw BusinessException.i18n(ResultCode.BUSINESS_ERROR, "validation.user.password.notblank");
        }
        if (!user.matchesPassword(oldPassword)) {
            throw BusinessException.i18n(ResultCode.BUSINESS_ERROR, "validation.user.oldpassword.wrong");
        }
        if (user.matchesPassword(newPassword)) {
            throw BusinessException.i18n(ResultCode.BUSINESS_ERROR, "validation.user.password.same");
        }
        user.changePassword(newPassword);
        user.setPwdUpdateDate(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    public void updateAvatar(SysUserAggregate user, String avatarUrl) {
        user.setAvatar(avatarUrl);
        userRepository.save(user);
    }
}
