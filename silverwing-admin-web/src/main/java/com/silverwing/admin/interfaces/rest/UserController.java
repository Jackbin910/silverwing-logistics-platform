package com.silverwing.admin.interfaces.rest;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.silverwing.admin.application.command.CreateUserCommand;
import com.silverwing.admin.application.command.UpdateUserCommand;
import com.silverwing.admin.application.service.UserAppService;
import com.silverwing.common.domain.PageResult;
import com.silverwing.common.domain.Result;
import com.silverwing.common.domain.model.SysUser;
import com.silverwing.common.domain.model.UserQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理接口（薄控制器）
 */
@Slf4j
@Tag(name = "用户管理", description = "用户增删改查接口")
@RestController
@RequestMapping("/admin/user")
@RequiredArgsConstructor
public class UserController {

    private final UserAppService userAppService;

    @SaCheckPermission("system:user:list")
    @Operation(summary = "分页查询用户列表")
    @GetMapping("/list")
    public Result<PageResult<SysUser>> list(UserQuery query) {
        return Result.success(userAppService.list(query));
    }

    @SaCheckPermission("system:user:query")
    @Operation(summary = "根据ID查询用户")
    @GetMapping("/{id}")
    public Result<SysUser> getById(@PathVariable Long id) {
        return Result.success(userAppService.getById(id));
    }

    @SaCheckPermission("system:user:add")
    @Operation(summary = "新建用户")
    @PostMapping
    public Result<SysUser> create(@Valid @RequestBody CreateUserCommand command) {
        return Result.success(userAppService.create(command));
    }

    @SaCheckPermission("system:user:edit")
    @Operation(summary = "更新用户信息")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody UpdateUserCommand command) {
        userAppService.update(id, command);
        return Result.success("更新成功");
    }

    @SaCheckPermission("system:user:delete")
    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userAppService.delete(id);
        return Result.success("删除成功");
    }

    @SaCheckPermission("system:user:resetPwd")
    @Operation(summary = "重置密码")
    @PutMapping("/{id}/password")
    public Result<Void> resetPassword(@PathVariable Long id,
                                       @RequestParam String newPassword) {
        userAppService.resetPassword(id, newPassword);
        return Result.success("重置成功");
    }

    @SaCheckPermission("system:user:edit")
    @Operation(summary = "启用/禁用用户")
    @PutMapping("/{id}/status")
    public Result<Void> toggleStatus(@PathVariable Long id) {
        userAppService.toggleStatus(id);
        return Result.success("操作成功");
    }

    @SaCheckPermission("system:user:assignRole")
    @Operation(summary = "为用户分配角色")
    @PutMapping("/{id}/roles")
    public Result<Void> assignRoles(@PathVariable Long id, @RequestBody List<Long> roleIds) {
        userAppService.assignRoles(id, roleIds);
        return Result.success("分配成功");
    }

    @SaCheckPermission("system:user:query")
    @Operation(summary = "查询用户已分配的角色ID列表")
    @GetMapping("/{id}/roles")
    public Result<List<Long>> getUserRoles(@PathVariable Long id) {
        return Result.success(userAppService.getUserRoleIds(id));
    }
}
