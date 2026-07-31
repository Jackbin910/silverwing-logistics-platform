package com.silverwing.admin.trigger.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.silverwing.common.annotation.Log;
import com.silverwing.common.enums.BusinessTypeEnum;
import com.silverwing.admin.application.command.ChangeRoleStatusCommand;
import com.silverwing.admin.application.command.RoleCommandService;
import com.silverwing.admin.application.command.RoleUserCommand;
import com.silverwing.admin.application.command.SaveRoleCommand;
import com.silverwing.admin.application.command.UpdateRoleDataScopeCommand;
import com.silverwing.admin.application.dto.DeptRoleTreeResponse;
import com.silverwing.admin.application.dto.RoleResponse;
import com.silverwing.admin.application.dto.UserResponse;
import com.silverwing.admin.application.query.RolePageQuery;
import com.silverwing.admin.application.query.RoleQueryService;
import com.silverwing.admin.application.query.UserPageQuery;
import com.silverwing.admin.trigger.dto.RoleExportVO;
import com.silverwing.common.domain.PageResult;
import com.silverwing.common.domain.Result;
import com.silverwing.common.util.ExcelUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
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
    @Operation(summary = "批量删除角色")
    @DeleteMapping("/{ids}")
    public Result<Void> delete(@PathVariable Long[] ids) {
        roleCommandService.deleteByIds(Arrays.asList(ids));
        return Result.success("删除成功");
    }

    @SaCheckPermission("system:role:assignPerm")
    @Operation(summary = "为角色分配权限")
    @PutMapping("/{id}/permissions")
    public Result<Void> assignPermissions(@PathVariable("id") Long id,
                                           @RequestBody List<Long> permissionIds) {
        roleCommandService.assignPermissions(id, permissionIds);
        return Result.success("分配成功");
    }

    @SaCheckPermission("system:role:query")
    @Operation(summary = "查询角色已分配的权限ID列表")
    @GetMapping("/{id}/permissions")
    public Result<List<Long>> getRolePermissions(@PathVariable("id") Long id) {
        return Result.success(roleQueryService.getRolePermissionIds(id));
    }

    @SaCheckPermission("system:role:export")
    @Log(title = "角色管理", businessType = BusinessTypeEnum.EXPORT)
    @Operation(summary = "导出角色数据")
    @PostMapping("/export")
    public void export(HttpServletResponse response) throws Exception {
        List<RoleResponse> list = roleQueryService.listAll();
        List<RoleExportVO> data = list.stream().map(r -> {
            RoleExportVO vo = new RoleExportVO();
            BeanUtils.copyProperties(r, vo);
            return vo;
        }).toList();
        ExcelUtils.export(response, data, RoleExportVO.class, "admin.role.export.name");
    }

    @SaCheckPermission("system:role:edit")
    @Operation(summary = "修改角色状态")
    @PutMapping("/changeStatus")
    public Result<Void> changeStatus(@RequestBody ChangeRoleStatusCommand command) {
        roleCommandService.changeStatus(command.getRoleId(), command.getStatus());
        return Result.success("修改成功");
    }

    @SaCheckPermission("system:role:edit")
    @Operation(summary = "修改角色数据范围")
    @PutMapping("/dataScope")
    public Result<Void> dataScope(@RequestBody UpdateRoleDataScopeCommand command) {
        roleCommandService.updateDataScope(command.getRoleId(), command.getDataScope(), command.getDeptIds());
        return Result.success("修改成功");
    }

    @SaCheckPermission("system:role:query")
    @Operation(summary = "查询角色已分配的用户列表")
    @GetMapping("/authUser/allocatedList")
    public Result<PageResult<UserResponse>> allocatedList(Long roleId, UserPageQuery query) {
        return Result.success(roleQueryService.allocatedList(roleId, query));
    }

    @SaCheckPermission("system:role:query")
    @Operation(summary = "查询角色未分配的用户列表")
    @GetMapping("/authUser/unallocatedList")
    public Result<PageResult<UserResponse>> unallocatedList(Long roleId, UserPageQuery query) {
        return Result.success(roleQueryService.unallocatedList(roleId, query));
    }

    @SaCheckPermission("system:role:edit")
    @Operation(summary = "取消角色与用户的授权")
    @PutMapping("/authUser/cancel")
    public Result<Void> cancelAuthUser(@RequestBody RoleUserCommand command) {
        roleCommandService.cancelAuthUser(command.getRoleId(), command.getUserId());
        return Result.success("取消授权成功");
    }

    @SaCheckPermission("system:role:edit")
    @Operation(summary = "批量取消角色与用户的授权")
    @PutMapping("/authUser/cancelAll")
    public Result<Void> cancelAuthAll(@RequestBody RoleUserCommand command) {
        roleCommandService.cancelAuthUsers(command.getRoleId(), command.getUserIds());
        return Result.success("取消授权成功");
    }

    @SaCheckPermission("system:role:edit")
    @Operation(summary = "批量授予角色用户")
    @PutMapping("/authUser/selectAll")
    public Result<Void> selectAuthAll(@RequestBody RoleUserCommand command) {
        roleCommandService.selectAuthUsers(command.getRoleId(), command.getUserIds());
        return Result.success("授权成功");
    }

    @SaCheckPermission("system:role:query")
    @Operation(summary = "获取角色部门树（勾选节点 + 部门树）")
    @GetMapping("/deptTree/{roleId}")
    public Result<DeptRoleTreeResponse> roleDeptTree(@PathVariable Long roleId) {
        return Result.success(roleQueryService.roleDeptTree(roleId));
    }
}
