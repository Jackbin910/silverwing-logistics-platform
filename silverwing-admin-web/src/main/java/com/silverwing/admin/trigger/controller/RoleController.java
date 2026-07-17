package com.silverwing.admin.trigger.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.silverwing.common.annotation.Log;
import com.silverwing.admin.application.command.RoleCommandService;
import com.silverwing.admin.application.command.SaveRoleCommand;
import com.silverwing.admin.application.dto.RoleResponse;
import com.silverwing.admin.application.query.RolePageQuery;
import com.silverwing.admin.application.query.RoleQueryService;
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
 * 角色管理接口（薄控制器，仅做 HTTP 转换与路由）
 * <p>读侧返回经由 RoleConvertor 映射的 {@link RoleResponse}，不再直接暴露领域聚合根。</p>
 */
@Slf4j
@RestController
@Tag(name = "角色管理", description = "角色增删改查接口")
@RequestMapping("/role")
@RequiredArgsConstructor
public class RoleController {

    private final RoleCommandService roleCommandService;
    private final RoleQueryService roleQueryService;

    @SaCheckPermission("system:role:list")
    @Operation(summary = "分页查询角色列表")
    @GetMapping("/list")
    public Result<PageResult<RoleResponse>> list(RolePageQuery query) {
        return Result.success(roleQueryService.list(query));
    }

    @SaCheckPermission("system:role:list")
    @Operation(summary = "查询全部启用角色")
    @GetMapping("/all")
    public Result<List<RoleResponse>> listAllEnabled() {
        return Result.success(roleQueryService.listAllEnabled());
    }

    @SaCheckPermission("system:role:query")
    @Operation(summary = "根据ID查询角色")
    @GetMapping("/{id}")
    public Result<RoleResponse> getById(@PathVariable Long id) {
        return Result.success(roleQueryService.getById(id));
    }

    @SaCheckPermission("system:role:add")
    @Operation(summary = "新建角色")
    @PostMapping
    public Result<RoleResponse> create(@Valid @RequestBody SaveRoleCommand command) {
        return Result.success(roleCommandService.create(command));
    }

    @SaCheckPermission("system:role:edit")
    @Operation(summary = "更新角色")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody SaveRoleCommand command) {
        roleCommandService.update(id, command);
        return Result.success("更新成功");
    }

    @SaCheckPermission("system:role:delete")
    @Operation(summary = "删除角色")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        roleCommandService.delete(id);
        return Result.success("删除成功");
    }

    @SaCheckPermission("system:role:assignPerm")
    @Operation(summary = "为角色分配权限")
    @PutMapping("/{id}/permissions")
    public Result<Void> assignPermissions(@PathVariable Long id,
                                           @RequestBody List<Long> permissionIds) {
        roleCommandService.assignPermissions(id, permissionIds);
        return Result.success("分配成功");
    }

    @SaCheckPermission("system:role:query")
    @Operation(summary = "查询角色已分配的权限ID列表")
    @GetMapping("/{id}/permissions")
    public Result<List<Long>> getRolePermissions(@PathVariable Long id) {
        return Result.success(roleQueryService.getRolePermissionIds(id));
    }
}
