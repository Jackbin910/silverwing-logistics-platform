package com.silverwing.auth.trigger.controller;

import com.silverwing.auth.application.command.AuthCommandService;
import com.silverwing.auth.application.command.LoginCommand;
import com.silverwing.auth.application.dto.AuthUserInfo;
import com.silverwing.auth.application.dto.LoginResponse;
import com.silverwing.auth.application.query.AuthQueryService;
import com.silverwing.common.annotation.SkipAuth;
import com.silverwing.common.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口（薄控制器，仅做 HTTP 转换与路由，不含业务编排）
 * <p>
 * 命令类用例委托给 application/command，查询类用例委托给 application/query。
 * </p>
 */
@Slf4j
@RestController
@Tag(name = "认证管理", description = "用户登录、登出等认证接口")
@RequiredArgsConstructor
public class AuthController {

    private final AuthCommandService authCommandService;
    private final AuthQueryService authQueryService;

    @SkipAuth
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginCommand command) {
        return Result.success(authCommandService.login(command));
    }

    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    public Result<Void> logout() {
        authCommandService.logout();
        return Result.success("登出成功");
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/userInfo")
    public Result<AuthUserInfo> getUserInfo() {
        return Result.success(authQueryService.getCurrentUserInfo());
    }

    @SkipAuth
    @Operation(summary = "刷新用户权限缓存（内部调用）")
    @PostMapping("/permission/refresh/{userId}")
    public Result<Void> refreshPermissionCache(@PathVariable("userId") Long userId) {
        authCommandService.refreshPermissionCache(userId);
        return Result.success("刷新成功");
    }
}
