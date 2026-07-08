package com.silverwing.auth.application.service;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import com.silverwing.auth.application.command.LoginCommand;
import com.silverwing.auth.application.dto.AuthUserInfo;
import com.silverwing.auth.application.dto.LoginResponse;
import com.silverwing.common.constant.SaSessionConstants;
import com.silverwing.biz.iam.domain.model.SysRole;
import com.silverwing.biz.iam.domain.model.SysUser;
import com.silverwing.biz.iam.domain.repository.PermissionRepository;
import com.silverwing.biz.iam.domain.repository.RoleRepository;
import com.silverwing.biz.iam.domain.repository.UserRepository;
import com.silverwing.common.domain.ResultCode;
import com.silverwing.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 认证应用服务
 * <p>
 * 编排登录、登出、获取当前用户信息、刷新权限缓存等用例。
 * 通过 Repository 接口访问领域数据，不直接依赖 Mapper。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthAppService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    /**
     * 用户登录
     * 流程：查询用户 → 状态校验 → MD5 + 盐 密码比对 → Sa-Token 签发 → 写入角色/权限到 Session
     */
    public LoginResponse login(LoginCommand command) {
        // 1. 查询用户
        SysUser user = userRepository.findByUsername(command.getUsername());
        if (user == null) {
            log.warn("登录失败：用户不存在 username={}", command.getUsername());
            throw new BusinessException(ResultCode.UNAUTHORIZED,
                    "auth.login.username.or.password.error");
        }

        // 2. 状态校验（领域行为）
        if (!user.isActive()) {
            log.warn("登录失败：用户已禁用 username={}", command.getUsername());
            throw new BusinessException(ResultCode.FORBIDDEN,
                    "auth.login.account.disabled");
        }

        // 3. 密码比对
        if (!matchesPassword(command.getPassword(), user)) {
            log.warn("登录失败：密码错误 username={}", command.getUsername());
            throw new BusinessException(ResultCode.UNAUTHORIZED,
                    "auth.login.username.or.password.error");
        }

        // 4. 查询角色和权限
        List<SysRole> roles = roleRepository.findRolesByUserId(user.getId());
        List<String> roleCodes = roles.stream()
                .map(SysRole::getRoleCode)
                .collect(Collectors.toList());
        List<String> permissions = permissionRepository.findPermissionCodesByUserId(user.getId());

        // 5. Sa-Token 登录，写入 Session
        StpUtil.login(user.getId());
        SaSession session = StpUtil.getSession();
        session.set(SaSessionConstants.ROLE_LIST, roleCodes);
        session.set(SaSessionConstants.PERMISSION_LIST, permissions);

        log.info("登录成功：username={}, userId={}, roles={}, 权限数={}",
                user.getUsername(), user.getId(), roleCodes, permissions.size());

        return LoginResponse.builder()
                .token(StpUtil.getTokenValue())
                .username(user.getUsername())
                .roles(roleCodes)
                .build();
    }

    /**
     * 用户登出
     */
    public void logout() {
        StpUtil.logout();
    }

    /**
     * 获取当前登录用户信息
     */
    public AuthUserInfo getCurrentUserInfo() {
        Object loginId = StpUtil.getLoginIdDefaultNull();
        if (loginId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "auth.not.login");
        }

        Long userId = Long.parseLong(loginId.toString());
        SysUser user = userRepository.findById(userId);

        List<SysRole> roles = roleRepository.findRolesByUserId(userId);
        List<String> roleCodes = roles.stream()
                .map(SysRole::getRoleCode)
                .collect(Collectors.toList());
        List<String> permissions = permissionRepository.findPermissionCodesByUserId(userId);

        if (user == null) {
            // 降级：返回基础信息
            return AuthUserInfo.builder()
                    .id(userId)
                    .username(loginId.toString())
                    .roles(roleCodes)
                    .permissions(permissions)
                    .build();
        }

        return AuthUserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .avatar(user.getAvatar())
                .email(user.getEmail())
                .phone(user.getPhone())
                .roles(roleCodes)
                .permissions(permissions)
                .build();
    }

    /**
     * 刷新指定用户的权限缓存（Sa-Token Session）
     * 在 admin-web 修改了角色/权限后调用。
     */
    public void refreshPermissionCache(Long userId) {
        try {
            List<String> permissionCodes = permissionRepository.findPermissionCodesByUserId(userId);
            List<SysRole> roles = roleRepository.findRolesByUserId(userId);
            List<String> roleCodes = roles.stream()
                    .map(SysRole::getRoleCode)
                    .collect(Collectors.toList());

            SaSession session = StpUtil.getSessionByLoginId(userId);
            session.set(SaSessionConstants.PERMISSION_LIST, permissionCodes);
            session.set(SaSessionConstants.ROLE_LIST, roleCodes);

            log.info("刷新用户权限缓存成功 userId={}, 权限数={}, 角色数={}",
                    userId, permissionCodes.size(), roleCodes.size());
        } catch (Exception e) {
            log.warn("刷新用户权限缓存失败 userId={}：{}", userId, e.getMessage());
        }
    }

    /**
     * 密码安全校验（MD5 + 盐）
     */
    private boolean matchesPassword(String rawPassword, SysUser user) {
        String encodedPassword = user.getPassword();
        if (encodedPassword == null || encodedPassword.isBlank()) {
            log.error("登录失败：用户密码哈希为空 userId={}, username={}",
                    user.getId(), user.getUsername());
            throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR,
                    "auth.login.account.config.error");
        }
        return user.matchesPassword(rawPassword);
    }
}
