package com.silverwing.admin.interfaces.rest;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.silverwing.admin.application.command.SavePermissionCommand;
import com.silverwing.admin.application.service.PermissionAppService;
import com.silverwing.common.domain.Result;
import com.silverwing.biz.iam.domain.model.SysPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 权限管理接口（薄控制器）
 */
@Slf4j
@Tag(name = "权限管理", description = "权限增删改查接口")
@RestController
@RequestMapping("/admin/permission")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionAppService permissionAppService;

    @SaCheckPermission("system:permission:list")
    @Operation(summary = "查询全部权限列表")
    @GetMapping("/list")
    public Result<List<SysPermission>> list() {
        return Result.success(permissionAppService.listAll());
    }

    @SaCheckPermission("system:permission:query")
    @Operation(summary = "根据ID查询权限")
    @GetMapping("/{id}")
    public Result<SysPermission> getById(@PathVariable Long id) {
        return Result.success(permissionAppService.getById(id));
    }

    @SaCheckPermission("system:permission:add")
    @Operation(summary = "新建权限")
    @PostMapping
    public Result<SysPermission> create(@Valid @RequestBody SavePermissionCommand command) {
        return Result.success(permissionAppService.create(command));
    }

    @SaCheckPermission("system:permission:edit")
    @Operation(summary = "更新权限")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody SavePermissionCommand command) {
        permissionAppService.update(id, command);
        return Result.success("更新成功");
    }

    @SaCheckPermission("system:permission:delete")
    @Operation(summary = "删除权限")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        permissionAppService.delete(id);
        return Result.success("删除成功");
    }

    @SaCheckPermission("system:permission:manage")
    @Operation(summary = "刷新用户权限缓存")
    @PostMapping("/refresh/{userId}")
    public Result<Void> refreshUserCache(@PathVariable Long userId) {
        permissionAppService.refreshUserPermissionCache(userId);
        return Result.success("刷新成功");
    }

    @SaCheckPermission("system:permission:manage")
    @Operation(summary = "批量刷新角色下用户权限缓存")
    @PostMapping("/refresh/role/{roleId}")
    public Result<Void> refreshRoleCache(@PathVariable Long roleId) {
        permissionAppService.refreshRoleUserCache(roleId);
        return Result.success("刷新成功");
    }
}
