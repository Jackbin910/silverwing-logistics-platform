package com.silverwing.admin.trigger.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.silverwing.admin.application.command.CreateUserCommand;
import com.silverwing.admin.application.command.UpdateUserCommand;
import com.silverwing.admin.application.command.UserCommandService;
import com.silverwing.admin.application.query.UserQueryService;
import com.silverwing.biz.iam.domain.model.aggregate.SysUserAggregate;
import com.silverwing.biz.iam.domain.model.query.UserQuery;
import com.silverwing.common.domain.PageResult;
import com.silverwing.common.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理接口（薄控制器，仅做 HTTP 转换与路由）
 * <p>命令类用例委托 application/command，查询类用例委托 application/query。</p>
 */
@Slf4j
@RestController
@Tag(name = "用户管理", description = "用户增删改查接口")
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserCommandService userCommandService;
    private final UserQueryService userQueryService;

    @SaCheckPermission("system:user:list")
    @Operation(summary = "分页查询用户列表")
    @GetMapping("/list")
    public Result<PageResult<SysUserAggregate>> list(UserQuery query) {
        return Result.success(userQueryService.list(query));
    }

    @SaCheckPermission("system:user:query")
    @Operation(summary = "根据ID查询用户")
    @GetMapping("/{id}")
    public Result<SysUserAggregate> getById(@PathVariable Long id) {
        return Result.success(userQueryService.getById(id));
    }

    @SaCheckPermission("system:user:add")
    @Operation(summary = "新建用户")
    @PostMapping
    public Result<SysUserAggregate> create(@Valid @RequestBody CreateUserCommand command) {
        return Result.success(userCommandService.create(command));
    }

    @SaCheckPermission("system:user:edit")
    @Operation(summary = "更新用户信息")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody UpdateUserCommand command) {
        userCommandService.update(id, command);
        return Result.success("更新成功");
    }

    @SaCheckPermission("system:user:delete")
    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userCommandService.delete(id);
        return Result.success("删除成功");
    }

    @SaCheckPermission("system:user:resetPwd")
    @Operation(summary = "重置密码")
    @PutMapping("/{id}/password")
    public Result<Void> resetPassword(@PathVariable Long id,
                                       @RequestParam String newPassword) {
        userCommandService.resetPassword(id, newPassword);
        return Result.success("重置成功");
    }

    @SaCheckPermission("system:user:edit")
    @Operation(summary = "启用/禁用用户")
    @PutMapping("/{id}/status")
    public Result<Void> toggleStatus(@PathVariable Long id) {
        userCommandService.toggleStatus(id);
        return Result.success("操作成功");
    }

    @SaCheckPermission("system:user:assignRole")
    @Operation(summary = "为用户分配角色")
    @PutMapping("/{id}/roles")
    public Result<Void> assignRoles(@PathVariable Long id, @RequestBody List<Long> roleIds) {
        userCommandService.assignRoles(id, roleIds);
        return Result.success("分配成功");
    }

    @SaCheckPermission("system:user:query")
    @Operation(summary = "查询用户已分配的角色ID列表")
    @GetMapping("/{id}/roles")
    public Result<List<Long>> getUserRoles(@PathVariable Long id) {
        return Result.success(userQueryService.getUserRoleIds(id));
    }
}
