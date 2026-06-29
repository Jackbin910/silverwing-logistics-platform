package com.silverwing.auth.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.silverwing.auth.entity.SysPermission;
import com.silverwing.auth.service.SysPermissionService;
import com.silverwing.common.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 权限管理控制器
 * 提供权限列表查询、角色权限分配等接口
 */
@Slf4j
@Tag(name = "权限管理", description = "权限列表、角色权限分配等接口")
@RestController
@RequestMapping("/permission")
@RequiredArgsConstructor
public class PermissionController {

    private final SysPermissionService sysPermissionService;

    /**
     * 查询全部权限列表
     */
    @SaCheckPermission("system:permission:manage")
    @Operation(summary = "查询全部权限")
    @GetMapping("/list")
    public Result<List<SysPermission>> list() {
        List<SysPermission> list = sysPermissionService.listAll();
        return Result.success(list);
    }

    /**
     * 查询角色已分配的权限ID列表
     */
    @SaCheckPermission("system:permission:manage")
    @Operation(summary = "查询角色权限")
    @GetMapping("/role/{roleId}")
    public Result<List<Long>> getRolePermissions(@PathVariable Long roleId) {
        List<Long> permissionIds = sysPermissionService.getPermissionIdsByRoleId(roleId);
        return Result.success(permissionIds);
    }

    /**
     * 为角色分配权限
     *
     * @param roleId        角色ID
     * @param permissionIds 权限ID列表（全量覆盖）
     */
    @SaCheckPermission("system:permission:manage")
    @Operation(summary = "分配角色权限")
    @PutMapping("/role/{roleId}")
    public Result<Void> assignPermissions(@PathVariable Long roleId,
                                          @RequestBody List<Long> permissionIds) {
        sysPermissionService.assignPermissionsToRole(roleId, permissionIds);
        return Result.success("分配成功");
    }

    /**
     * 手动刷新指定用户的权限缓存（Session）
     * 用于权限变更后即时生效
     */
    @SaCheckPermission("system:permission:manage")
    @Operation(summary = "刷新用户权限缓存")
    @PostMapping("/refresh/{userId}")
    public Result<Void> refreshUserCache(@PathVariable Long userId) {
        sysPermissionService.refreshUserPermissionCache(userId);
        return Result.success("刷新成功");
    }

}
