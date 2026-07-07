package com.silverwing.auth.controller;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.silverwing.auth.domain.dto.LoginRequest;
import com.silverwing.auth.domain.dto.LoginResponse;
import com.silverwing.auth.domain.dto.UserInfo;
import com.silverwing.auth.entity.SysRole;
import com.silverwing.auth.entity.SysUser;
import com.silverwing.auth.service.SysPermissionService;
import com.silverwing.auth.service.SysRoleService;
import com.silverwing.auth.service.SysUserService;
import com.silverwing.common.annotation.SkipAuth;
import com.silverwing.common.constant.SaSessionConstants;
import com.silverwing.common.domain.Result;
import com.silverwing.common.domain.ResultCode;
import com.silverwing.common.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 认证控制器
 */
@Slf4j
@Tag(name = "认证管理", description = "用户登录、登出等认证接口")
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final SysUserService sysUserService;
    private final SysRoleService sysRoleService;
    private final SysPermissionService sysPermissionService;

    /**
     * 登录接口
     * 流程：查询数据库 → BCrypt 密码比对 → Sa-Token 签发 token → 写入角色/权限到 Session
     *
     * @param request 登录请求
     * @return 登录响应（含 token、用户基本信息）
     */
    @SkipAuth
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        // 1. 从数据库查询用户
        SysUser user = sysUserService.getByUsername(request.getUsername());
        if (user == null) {
            log.warn("登录失败：用户不存在 username={}", request.getUsername());
            throw new BusinessException(ResultCode.UNAUTHORIZED,
                    "auth.login.username.or.password.error");
        }

        // 2. 检查用户状态
        if (user.getStatus() != null && user.getStatus() == 0) {
            log.warn("登录失败：用户已禁用 username={}", request.getUsername());
            throw new BusinessException(ResultCode.FORBIDDEN,
                    "auth.login.account.disabled");
        }

        // 3. BCrypt 密码比对
        if (!matchesPassword(request.getPassword(), user)) {
            log.warn("登录失败：密码错误 username={}", request.getUsername());
            throw new BusinessException(ResultCode.UNAUTHORIZED,
                    "auth.login.username.or.password.error");
        }

        // 4. 查询用户角色
        List<SysRole> roles = sysRoleService.getRolesByUserId(user.getId());
        List<String> roleCodes = roles.stream()
                .map(SysRole::getRoleCode)
                .collect(Collectors.toList());

        // 5. 查询用户权限标识
        List<String> permissions = sysPermissionService.getPermissionCodesByUserId(user.getId());

        // 6. Sa-Token 登录，签发 token，并将角色、权限写入 Session（Redis 共享）
        StpUtil.login(user.getId());
        SaSession session = StpUtil.getSession();
        session.set(SaSessionConstants.ROLE_LIST, roleCodes);
        session.set(SaSessionConstants.PERMISSION_LIST, permissions);
        String token = StpUtil.getTokenValue();

        log.info("登录成功：username={}, userId={}, roles={}, 权限数={}",
                user.getUsername(), user.getId(), roleCodes, permissions.size());

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUsername(user.getUsername());
        response.setRoles(roleCodes);
        return Result.success(response);
    }

    /**
     * 登出接口（需要登录后才能调用）
     */
    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    public Result<Void> logout() {
        StpUtil.logout();
        return Result.success("登出成功");
    }

    /**
     * 获取当前登录用户信息（从数据库加载真实数据，含角色与权限）
     */
    @Operation(summary = "获取当前用户信息")
    @GetMapping("/userInfo")
    public Result<UserInfo> getUserInfo() {
        Object loginId = StpUtil.getLoginIdDefaultNull();
        if (loginId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "auth.not.login");
        }

        Long userId = Long.parseLong(loginId.toString());

        // 从数据库查询用户信息、角色与权限
        SysUser user = sysUserService.getById(userId);
        List<SysRole> roles = sysRoleService.getRolesByUserId(userId);
        List<String> roleCodes = roles.stream()
                .map(SysRole::getRoleCode)
                .collect(Collectors.toList());
        List<String> permissions = sysPermissionService.getPermissionCodesByUserId(userId);

        if (user == null) {
            // 降级：如果查不到，返回基础信息
            UserInfo userInfo = new UserInfo();
            userInfo.setId(userId);
            userInfo.setUsername(loginId.toString());
            userInfo.setRoles(roleCodes);
            userInfo.setPermissions(permissions);
            return Result.success(userInfo);
        }

        UserInfo userInfo = new UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setAvatar(user.getAvatar());
        userInfo.setEmail(user.getEmail());
        userInfo.setPhone(user.getPhone());
        userInfo.setRoles(roleCodes);
        userInfo.setPermissions(permissions);
        return Result.success(userInfo);
    }

    /**
     * 安全校验用户密码，避免数据库中异常 BCrypt 哈希触发底层栈溢出。
     *
     * @param rawPassword 前端提交的明文密码
     * @param user        当前用户实体
     * @return true 表示密码匹配
     */
    private boolean matchesPassword(String rawPassword, SysUser user) {
        try {
            String encodedPassword = user.getPassword();
            if (encodedPassword == null || encodedPassword.isBlank()) {
                log.error("登录失败：用户密码哈希为空 userId={}, username={}",
                        user.getId(), user.getUsername());
                throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR,
                        "auth.login.account.config.error");
            }
            return BCrypt.checkpw(rawPassword, encodedPassword);
        } catch (StackOverflowError e) {
            log.error("登录失败：BCrypt 密码哈希触发栈溢出 userId={}, username={}",
                    user.getId(), user.getUsername(), e);
            throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR,
                    "auth.login.account.config.error");
        } catch (IllegalArgumentException e) {
            log.error("登录失败：BCrypt 密码哈希格式非法 userId={}, username={}",
                    user.getId(), user.getUsername(), e);
            throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR,
                    "auth.login.account.config.error");
        }
    }

}
