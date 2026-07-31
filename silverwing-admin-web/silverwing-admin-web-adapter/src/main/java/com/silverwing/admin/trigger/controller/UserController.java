package com.silverwing.admin.trigger.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.alibaba.excel.EasyExcel;
import com.silverwing.admin.application.command.CreateUserCommand;
import com.silverwing.admin.application.command.UpdateUserCommand;
import com.silverwing.admin.application.command.UserCommandService;
import com.silverwing.admin.application.command.UserImportCommand;
import com.silverwing.admin.application.dto.DeptTreeResponse;
import com.silverwing.admin.application.dto.UserResponse;
import com.silverwing.admin.application.query.UserPageQuery;
import com.silverwing.admin.application.query.UserQueryService;
import com.silverwing.admin.client.DeptClient;
import com.silverwing.common.annotation.Log;
import com.silverwing.common.domain.PageResult;
import com.silverwing.common.domain.Result;
import com.silverwing.common.enums.BusinessTypeEnum;
import com.silverwing.common.util.ExcelUtils;
import com.silverwing.admin.trigger.dto.UserExportVO;
import com.silverwing.admin.trigger.dto.UserImportVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户管理接口（薄控制器，仅做 HTTP 转换与路由）
 * <p>命令类用例委托 application/command，查询类用例委托 application/query；读侧返回
 * 经由 UserConvertor 映射的 {@link UserResponse}，不再直接暴露领域聚合根。</p>
 */
@Slf4j
@RestController
@Tag(name = "用户管理", description = "用户增删改查接口")
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserCommandService userCommandService;
    private final UserQueryService userQueryService;
    private final DeptClient deptClient;

    @SaCheckPermission("system:user:list")
    @Operation(summary = "分页查询用户列表")
    @GetMapping("/list")
    public Result<PageResult<UserResponse>> list(UserPageQuery query) {
        return Result.success(userQueryService.list(query));
    }

    @SaCheckPermission("system:user:query")
    @Operation(summary = "根据ID查询用户")
    @GetMapping("/{id}")
    public Result<UserResponse> getById(@PathVariable("id") Long id) {
        return Result.success(userQueryService.getById(id));
    }

    @Log(title = "用户管理-新建用户", businessType = BusinessTypeEnum.INSERT)
    @SaCheckPermission("system:user:add")
    @Operation(summary = "新建用户")
    @PostMapping
    public Result<UserResponse> create(@Valid @RequestBody CreateUserCommand command) {
        return Result.success(userCommandService.create(command));
    }

    @Log(title = "用户管理-更新用户", businessType = BusinessTypeEnum.UPDATE)
    @SaCheckPermission("system:user:edit")
    @Operation(summary = "更新用户信息")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable("id") Long id, @RequestBody UpdateUserCommand command) {
        userCommandService.update(id, command);
        return Result.success("更新成功");
    }

    @Log(title = "用户管理-删除用户", businessType = BusinessTypeEnum.DELETE)
    @SaCheckPermission("system:user:delete")
    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Long id) {
        userCommandService.delete(id);
        return Result.success("删除成功");
    }

    @Log(title = "用户管理-重置密码", businessType = BusinessTypeEnum.UPDATE)
    @SaCheckPermission("system:user:resetPwd")
    @Operation(summary = "重置密码")
    @PutMapping("/{id}/password")
    public Result<Void> resetPassword(@PathVariable("id") Long id,
                                       @RequestParam String newPassword) {
        userCommandService.resetPassword(id, newPassword);
        return Result.success("重置成功");
    }

    @SaCheckPermission("system:user:edit")
    @Operation(summary = "启用/禁用用户")
    @PutMapping("/{id}/status")
    public Result<Void> toggleStatus(@PathVariable("id") Long id) {
        userCommandService.toggleStatus(id);
        return Result.success("操作成功");
    }

    @SaCheckPermission("system:user:assignRole")
    @Operation(summary = "为用户分配角色")
    @PutMapping("/{id}/roles")
    public Result<Void> assignRoles(@PathVariable("id") Long id, @RequestBody List<Long> roleIds) {
        userCommandService.assignRoles(id, roleIds);
        return Result.success("分配成功");
    }

    @SaCheckPermission("system:user:query")
    @Operation(summary = "查询用户已分配的角色ID列表")
    @GetMapping("/{id}/roles")
    public Result<List<Long>> getUserRoles(@PathVariable("id") Long id) {
        return Result.success(userQueryService.getUserRoleIds(id));
    }

    @SaCheckPermission("system:user:assignPost")
    @Operation(summary = "为用户分配岗位")
    @PutMapping("/{id}/posts")
    public Result<Void> assignPosts(@PathVariable("id") Long id, @RequestBody List<Long> postIds) {
        userCommandService.assignPosts(id, postIds);
        return Result.success("分配成功");
    }

    @SaCheckPermission("system:user:query")
    @Operation(summary = "查询用户已分配的岗位ID列表")
    @GetMapping("/{id}/posts")
    public Result<List<Long>> getUserPosts(@PathVariable("id") Long id) {
        return Result.success(userQueryService.getUserPostIds(id));
    }

    @Log(title = "用户管理-导出", businessType = BusinessTypeEnum.EXPORT)
    @SaCheckPermission("system:user:export")
    @Operation(summary = "导出用户列表", description = "按查询条件导出 Excel")
    @PostMapping("/export")
    public void export(UserPageQuery query, HttpServletResponse response) {
        List<UserResponse> list = userQueryService.exportList(query);
        List<UserExportVO> data = list.stream().map(this::toExportVo).collect(Collectors.toList());
        ExcelUtils.export(response, data, UserExportVO.class, "admin.user.export.name");
    }

    @Log(title = "用户管理-导入模板", businessType = BusinessTypeEnum.EXPORT)
    @SaCheckPermission("system:user:import")
    @Operation(summary = "下载用户导入模板")
    @GetMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response) {
        ExcelUtils.export(response, new ArrayList<>(), UserImportVO.class, "admin.user.import.template.name");
    }

    @Log(title = "用户管理-导入", businessType = BusinessTypeEnum.IMPORT)
    @SaCheckPermission("system:user:import")
    @Operation(summary = "导入用户数据", description = "从 Excel 批量导入用户，支持覆盖更新已存在用户")
    @PostMapping("/importData")
    public Result<String> importData(@RequestParam("file") MultipartFile file,
                                     @RequestParam(value = "updateSupport", defaultValue = "false") boolean updateSupport)
            throws IOException {
        List<UserImportVO> rows = EasyExcel.read(file.getInputStream())
                .head(UserImportVO.class).sheet().doReadSync();
        List<UserImportCommand> commands = rows.stream().map(this::toImportCommand).collect(Collectors.toList());
        int success = userCommandService.importUsers(commands, updateSupport);
        return Result.success("成功导入 " + success + " 条用户");
    }

    @SaCheckPermission("system:user:list")
    @Operation(summary = "部门树（用户选择）", description = "对应 RuoYi SysUserController 的 deptTree")
    @GetMapping("/deptTree")
    public Result<List<DeptTreeResponse>> deptTree() {
        return Result.success(deptClient.treeSelect());
    }

    /**
     * 将用户响应映射为导出视图对象
     */
    private UserExportVO toExportVo(UserResponse user) {
        UserExportVO vo = new UserExportVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setSex(user.getSex());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setStatus(user.getStatus());
        vo.setDeptId(user.getDeptId());
        vo.setCreateTime(user.getCreateTime());
        return vo;
    }

    /**
     * 将导入视图对象映射为导入命令
     */
    private UserImportCommand toImportCommand(UserImportVO vo) {
        UserImportCommand command = new UserImportCommand();
        command.setUsername(vo.getUsername());
        command.setNickname(vo.getNickname());
        command.setSex(vo.getSex());
        command.setPhone(vo.getPhone());
        command.setEmail(vo.getEmail());
        command.setStatus(vo.getStatus());
        command.setDeptId(vo.getDeptId());
        return command;
    }
}
