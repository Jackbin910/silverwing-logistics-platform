package com.silverwing.auth.interfaces.rest;

import com.silverwing.auth.application.command.LoginCommand;
import com.silverwing.auth.application.dto.AuthUserInfo;
import com.silverwing.auth.application.dto.LoginResponse;
import com.silverwing.auth.application.service.AuthAppService;
import com.silverwing.common.annotation.SkipAuth;
import com.silverwing.common.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 认证接口（薄控制器，仅做 HTTP 转换）
 */
@Slf4j
@Tag(name = "认证管理", description = "用户登录、登出等认证接口")
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthAppService authAppService;

    @SkipAuth
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginCommand command) {
        return Result.success(authAppService.login(command));
    }

    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    public Result<Void> logout() {
        authAppService.logout();
        return Result.success("登出成功");
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/userInfo")
    public Result<AuthUserInfo> getUserInfo() {
        return Result.success(authAppService.getCurrentUserInfo());
    }

    @SkipAuth
    @Operation(summary = "刷新用户权限缓存（内部调用）")
    @PostMapping("/permission/refresh/{userId}")
    public Result<Void> refreshPermissionCache(@PathVariable Long userId) {
        authAppService.refreshPermissionCache(userId);
        return Result.success("刷新成功");
    }
}
