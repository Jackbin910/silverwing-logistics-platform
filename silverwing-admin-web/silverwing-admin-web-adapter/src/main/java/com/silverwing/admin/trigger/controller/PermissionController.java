package com.silverwing.admin.trigger.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.silverwing.common.annotation.Log;
import com.silverwing.common.enums.BusinessTypeEnum;
import com.silverwing.admin.application.command.PermissionCommandService;
import com.silverwing.admin.application.command.SavePermissionCommand;
import com.silverwing.admin.application.command.UpdatePermissionSortCommand;
import com.silverwing.admin.application.dto.PermissionResponse;
import com.silverwing.admin.application.dto.RolePermissionTreeSelectResponse;
import com.silverwing.admin.application.dto.RouterVo;
import com.silverwing.admin.application.dto.TreeSelect;
import com.silverwing.admin.application.query.PermissionPageQuery;
import com.silverwing.admin.application.query.PermissionQueryService;
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
    @Operation(summary = "分页查询权限列表", description = "支持关键词（权限编码/名称）与状态筛选")
    @GetMapping("/list")
    public Result<PageResult<PermissionResponse>> list(PermissionPageQuery query) {
        return Result.success(permissionQueryService.page(query));
    }

    @SaCheckPermission("system:permission:list")
    @Operation(summary = "查询全部权限", description = "用于分配权限等需全量展示的场景")
    @GetMapping("/all")
    public Result<List<PermissionResponse>> listAll() {
        return Result.success(permissionQueryService.listAll());
    }

    @SaCheckPermission("system:permission:query")
    @Operation(summary = "根据ID查询权限")
    @GetMapping("/{id}")
    public Result<PermissionResponse> getById(@PathVariable("id") Long id) {
        return Result.success(permissionQueryService.getById(id));
    }

    @Log(title = "权限管理-新建权限", businessType = BusinessTypeEnum.INSERT)
    @SaCheckPermission("system:permission:add")
    @Operation(summary = "新建权限")
    @PostMapping
    public Result<PermissionResponse> create(@Valid @RequestBody SavePermissionCommand command) {
        return Result.success(permissionCommandService.create(command));
    }

    @Log(title = "权限管理-更新权限", businessType = BusinessTypeEnum.UPDATE)
    @SaCheckPermission("system:permission:edit")
    @Operation(summary = "更新权限")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable("id") Long id, @Valid @RequestBody SavePermissionCommand command) {
        permissionCommandService.update(id, command);
        return Result.success("更新成功");
    }

    @Log(title = "权限管理-删除权限", businessType = BusinessTypeEnum.DELETE)
    @SaCheckPermission("system:permission:delete")
    @Operation(summary = "删除权限")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Long id) {
        permissionCommandService.delete(id);
        return Result.success("删除成功");
    }

    @SaCheckPermission("system:permission:manage")
    @Operation(summary = "刷新用户权限缓存")
    @PostMapping("/refresh/{userId}")
    public Result<Void> refreshUserCache(@PathVariable("userId") Long userId) {
        permissionCommandService.refreshUserPermissionCache(userId);
        return Result.success("刷新成功");
    }

    @SaCheckPermission("system:permission:manage")
    @Operation(summary = "批量刷新角色下用户权限缓存")
    @PostMapping("/refresh/role/{roleId}")
    public Result<Void> refreshRoleCache(@PathVariable("roleId") Long roleId) {
        permissionCommandService.refreshRoleUserCache(roleId);
        return Result.success("刷新成功");
    }

    @SaCheckPermission("system:permission:list")
    @Operation(summary = "权限树形下拉列表", description = "菜单管理中的上级菜单树选择")
    @GetMapping("/treeselect")
    public Result<List<TreeSelect>> treeSelect(PermissionPageQuery query) {
        return Result.success(permissionQueryService.treeSelect(query));
    }

    @SaCheckPermission("system:permission:list")
    @Operation(summary = "角色关联权限树", description = "返回角色已勾选权限ID与完整权限树，用于角色授权")
    @GetMapping("/rolePermissionTreeSelect/{roleId}")
    public Result<RolePermissionTreeSelectResponse> rolePermissionTreeSelect(@PathVariable("roleId") Long roleId) {
        return Result.success(permissionQueryService.rolePermissionTreeSelect(roleId));
    }

    @Log(title = "权限管理-保存排序", businessType = BusinessTypeEnum.UPDATE)
    @SaCheckPermission("system:permission:edit")
    @Operation(summary = "保存权限（菜单）排序", description = "按传入ID顺序递增排序值")
    @PutMapping("/updateSort")
    public Result<Void> updateSort(@Valid @RequestBody UpdatePermissionSortCommand command) {
        permissionCommandService.updateSort(command);
        return Result.success("保存成功");
    }

    @SaCheckPermission("system:permission:list")
    @Operation(summary = "获取登录用户前端路由菜单", description = "对应 RuoYi 的 getRouters，用于前端动态路由")
    @GetMapping("/getRouters")
    public Result<List<RouterVo>> getRouters() {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(permissionQueryService.getRouters(userId));
    }
}
