package com.silverwing.admin.trigger.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.silverwing.common.annotation.Log;
import com.silverwing.admin.application.command.PermissionCommandService;
import com.silverwing.admin.application.command.SavePermissionCommand;
import com.silverwing.admin.application.dto.PermissionResponse;
import com.silverwing.admin.application.query.PermissionQueryService;
import com.silverwing.common.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 权限管理接口（薄控制器，仅做 HTTP 转换与路由）
 * <p>读侧返回经由 PermissionConvertor 映射的 {@link PermissionResponse}，不再直接暴露领域聚合根。</p>
 */
@Slf4j
@RestController
@Tag(name = "权限管理", description = "权限增删改查接口")
@RequestMapping("/permission")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionCommandService permissionCommandService;
    private final PermissionQueryService permissionQueryService;

    @SaCheckPermission("system:permission:list")
    @Operation(summary = "查询全部权限列表")
    @GetMapping("/list")
    public Result<List<PermissionResponse>> list() {
        return Result.success(permissionQueryService.listAll());
    }

    @SaCheckPermission("system:permission:query")
    @Operation(summary = "根据ID查询权限")
    @GetMapping("/{id}")
    public Result<PermissionResponse> getById(@PathVariable Long id) {
        return Result.success(permissionQueryService.getById(id));
    }

    @Log(title = "权限管理-新建权限", businessType = 1)
    @SaCheckPermission("system:permission:add")
    @Operation(summary = "新建权限")
    @PostMapping
    public Result<PermissionResponse> create(@Valid @RequestBody SavePermissionCommand command) {
        return Result.success(permissionCommandService.create(command));
    }

    @Log(title = "权限管理-更新权限", businessType = 2)
    @SaCheckPermission("system:permission:edit")
    @Operation(summary = "更新权限")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody SavePermissionCommand command) {
        permissionCommandService.update(id, command);
        return Result.success("更新成功");
    }

    @Log(title = "权限管理-删除权限", businessType = 3)
    @SaCheckPermission("system:permission:delete")
    @Operation(summary = "删除权限")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        permissionCommandService.delete(id);
        return Result.success("删除成功");
    }

    @SaCheckPermission("system:permission:manage")
    @Operation(summary = "刷新用户权限缓存")
    @PostMapping("/refresh/{userId}")
    public Result<Void> refreshUserCache(@PathVariable Long userId) {
        permissionCommandService.refreshUserPermissionCache(userId);
        return Result.success("刷新成功");
    }

    @SaCheckPermission("system:permission:manage")
    @Operation(summary = "批量刷新角色下用户权限缓存")
    @PostMapping("/refresh/role/{roleId}")
    public Result<Void> refreshRoleCache(@PathVariable Long roleId) {
        permissionCommandService.refreshRoleUserCache(roleId);
        return Result.success("刷新成功");
    }
}
