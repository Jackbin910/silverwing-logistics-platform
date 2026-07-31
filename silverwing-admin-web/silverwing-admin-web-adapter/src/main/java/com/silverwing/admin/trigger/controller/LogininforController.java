package com.silverwing.admin.trigger.controller;

import com.silverwing.admin.application.command.LogininforCommandService;
import com.silverwing.admin.application.command.SaveLogininforCommand;
import com.silverwing.admin.application.dto.LogininforResponse;
import com.silverwing.admin.application.query.LogininforPageQuery;
import com.silverwing.admin.application.query.LogininforQueryService;
import com.silverwing.common.annotation.Log;
import com.silverwing.common.domain.PageResult;
import com.silverwing.common.domain.Result;
import com.silverwing.common.enums.BusinessTypeEnum;
import com.silverwing.common.util.ExcelUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

/**
 * 系统访问记录（登录日志）管理控制器。
 */
@Tag(name = "系统访问记录")
@RestController
@RequestMapping("/system/logininfor")
@RequiredArgsConstructor
public class LogininforController {

    private final LogininforQueryService logininforQueryService;
    private final LogininforCommandService logininforCommandService;

    @Operation(summary = "登录日志分页列表")
    @SaCheckPermission("system:logininfor:list")
    @GetMapping("/list")
    public Result<PageResult<LogininforResponse>> list(LogininforPageQuery query) {
        return Result.success(logininforQueryService.list(query));
    }

    @Operation(summary = "导出登录日志")
    @Log(title = "系统访问记录", businessType = BusinessTypeEnum.OTHER)
    @SaCheckPermission("system:logininfor:export")
    @PostMapping("/export")
    public void export(HttpServletResponse response, LogininforPageQuery query) throws IOException {
        List<LogininforResponse> list = logininforQueryService.listExport(query);
        ExcelUtils.export(response, list, LogininforResponse.class, "admin.logininfor.export.name");
    }

    @Operation(summary = "登录日志详情")
    @SaCheckPermission("system:logininfor:list")
    @GetMapping("/{infoId}")
    public Result<LogininforResponse> getById(@PathVariable Long infoId) {
        return Result.success(logininforQueryService.getById(infoId));
    }

    @Operation(summary = "新增登录日志（内部调用，用于记录登录行为）")
    @PostMapping
    public Result<Void> add(@RequestBody SaveLogininforCommand command) {
        logininforCommandService.add(command);
        return Result.success();
    }

    @Operation(summary = "删除登录日志")
    @Log(title = "系统访问记录", businessType = BusinessTypeEnum.DELETE)
    @SaCheckPermission("system:logininfor:remove")
    @DeleteMapping("/{infoIds}")
    public Result<Void> remove(@PathVariable Long[] infoIds) {
        logininforCommandService.removeByIds(List.of(infoIds));
        return Result.success();
    }

    @Operation(summary = "清空登录日志")
    @Log(title = "系统访问记录", businessType = BusinessTypeEnum.OTHER)
    @SaCheckPermission("system:logininfor:remove")
    @DeleteMapping("/clean")
    public Result<Void> clean() {
        logininforCommandService.clean();
        return Result.success();
    }

    @Operation(summary = "解锁用户账户")
    @Log(title = "系统访问记录", businessType = BusinessTypeEnum.OTHER)
    @SaCheckPermission("system:logininfor:unlock")
    @GetMapping("/unlock/{userName}")
    public Result<Void> unlock(@PathVariable String userName) {
        logininforCommandService.unlock(userName);
        return Result.success();
    }
}
