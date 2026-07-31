package com.silverwing.admin.trigger.controller;

import com.silverwing.admin.application.command.DeptCommandService;
import com.silverwing.admin.application.command.SaveDeptCommand;
import com.silverwing.admin.application.dto.DeptResponse;
import com.silverwing.admin.application.dto.DeptRoleTreeResponse;
import com.silverwing.admin.application.dto.DeptTreeResponse;
import com.silverwing.admin.application.query.DeptQuery;
import com.silverwing.admin.application.query.DeptQueryService;
import com.silverwing.common.annotation.Log;
import com.silverwing.common.domain.Result;
import com.silverwing.common.exception.BusinessException;
import com.silverwing.common.domain.ResultCode;
import com.silverwing.common.enums.BusinessTypeEnum;
import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 部门信息管理。
 * <p>
 * 接口路径与 RuoYi-Cloud 的 SysDeptController 保持一致，便于前端直接对接。
 * </p>
 */
@RestController
@RequestMapping("/dept")
@Tag(name = "部门信息管理")
@RequiredArgsConstructor
public class DeptController {

    private final DeptQueryService deptQueryService;
    private final DeptCommandService deptCommandService;

    @Operation(summary = "获取部门列表")
    @SaCheckPermission("system:dept:list")
    @GetMapping("/list")
    public Result<List<DeptResponse>> list(DeptQuery query) {
        return Result.success(deptQueryService.list(query));
    }

    @Operation(summary = "查询部门列表（排除节点及其子节点）")
    @SaCheckPermission("system:dept:list")
    @GetMapping("/list/exclude/{deptId}")
    public Result<List<DeptResponse>> excludeChild(@PathVariable Long deptId) {
        return Result.success(deptQueryService.listExcludeChild(deptId));
    }

    @Operation(summary = "根据部门编号获取详细信息")
    @SaCheckPermission("system:dept:query")
    @GetMapping("/{deptId}")
    public Result<DeptResponse> getInfo(@PathVariable Long deptId) {
        return Result.success(deptQueryService.getById(deptId));
    }

    @Operation(summary = "获取部门下拉树")
    @SaCheckPermission("system:dept:list")
    @GetMapping("/treeselect")
    public Result<List<DeptTreeResponse>> treeSelect() {
        return Result.success(deptQueryService.treeSelect());
    }

    @Operation(summary = "根据角色ID获取部门树（含已选节点）")
    @SaCheckPermission("system:dept:list")
    @GetMapping("/roleDeptTreeselect/{roleId}")
    public Result<DeptRoleTreeResponse> roleDeptTreeSelect(@PathVariable Long roleId) {
        return Result.success(deptQueryService.roleDeptTreeSelect(roleId));
    }

    @Operation(summary = "新增部门")
    @Log(title = "部门管理", businessType = BusinessTypeEnum.INSERT)
    @SaCheckPermission("system:dept:add")
    @PostMapping
    public Result<DeptResponse> add(@Valid @RequestBody SaveDeptCommand command) {
        return Result.success(deptCommandService.create(command));
    }

    @Operation(summary = "修改部门")
    @Log(title = "部门管理", businessType = BusinessTypeEnum.UPDATE)
    @SaCheckPermission("system:dept:edit")
    @PutMapping
    public Result<Void> edit(@Valid @RequestBody SaveDeptCommand command) {
        if (command.getDeptId() == null) {
            throw new BusinessException(ResultCode.PARAM_VALIDATE_ERROR, "admin.dept.id.required");
        }
        deptCommandService.update(command.getDeptId(), command);
        return Result.success();
    }

    @Operation(summary = "保存部门排序")
    @Log(title = "保存部门排序", businessType = BusinessTypeEnum.UPDATE)
    @SaCheckPermission("system:dept:edit")
    @PutMapping("/updateSort")
    public Result<Void> updateSort(@RequestBody Map<String, String> params) {
        String[] deptIds = params.get("deptIds").split(",");
        String[] orderNums = params.get("orderNums").split(",");
        for (int i = 0; i < deptIds.length; i++) {
            deptCommandService.saveSort(Long.valueOf(deptIds[i]), Integer.valueOf(orderNums[i]));
        }
        return Result.success();
    }

    @Operation(summary = "删除部门")
    @Log(title = "部门管理", businessType = BusinessTypeEnum.DELETE)
    @SaCheckPermission("system:dept:remove")
    @DeleteMapping("/{deptId}")
    public Result<Void> remove(@PathVariable Long deptId) {
        deptCommandService.delete(deptId);
        return Result.success();
    }
}
