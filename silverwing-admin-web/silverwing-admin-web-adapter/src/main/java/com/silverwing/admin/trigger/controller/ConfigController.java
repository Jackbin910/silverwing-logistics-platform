package com.silverwing.admin.trigger.controller;

import com.silverwing.admin.application.command.ConfigCommandService;
import com.silverwing.admin.application.command.SaveConfigCommand;
import com.silverwing.admin.application.dto.ConfigResponse;
import com.silverwing.admin.application.query.ConfigPageQuery;
import com.silverwing.admin.application.query.ConfigQueryService;
import com.silverwing.common.annotation.Log;
import com.silverwing.common.domain.PageResult;
import com.silverwing.common.domain.Result;
import com.silverwing.common.enums.BusinessTypeEnum;
import com.silverwing.common.util.ExcelUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

/**
 * 参数配置管理。
 */
@RestController
@RequestMapping("/config")
@Tag(name = "参数配置管理")
@RequiredArgsConstructor
public class ConfigController {

    private final ConfigQueryService configQueryService;
    private final ConfigCommandService configCommandService;

    @Operation(summary = "参数配置分页列表")
    @SaCheckPermission("system:config:list")
    @GetMapping("/list")
    public Result<PageResult<ConfigResponse>> list(ConfigPageQuery query) {
        return Result.success(configQueryService.list(query));
    }

    @Operation(summary = "导出参数配置")
    @Log(title = "参数管理", businessType = BusinessTypeEnum.OTHER)
    @SaCheckPermission("system:config:export")
    @PostMapping("/export")
    public void export(HttpServletResponse response, ConfigPageQuery query) throws IOException {
        List<ConfigResponse> list = configQueryService.listExport(query);
        ExcelUtils.export(response, list, ConfigResponse.class, "admin.config.export.name");
    }

    @Operation(summary = "参数配置详情")
    @SaCheckPermission("system:config:query")
    @GetMapping("/{id}")
    public Result<ConfigResponse> getById(@PathVariable Long id) {
        return Result.success(configQueryService.getById(id));
    }

    @Operation(summary = "根据参数键名查询参数值")
    @SaCheckPermission("system:config:query")
    @GetMapping("/configKey/{configKey}")
    public Result<String> getConfigKey(@PathVariable String configKey) {
        return Result.success(configQueryService.getByKey(configKey));
    }

    @Operation(summary = "新增参数配置")
    @Log(title = "参数管理", businessType = BusinessTypeEnum.INSERT)
    @SaCheckPermission("system:config:add")
    @PostMapping
    public Result<ConfigResponse> create(@Valid @RequestBody SaveConfigCommand command) {
        return Result.success(configCommandService.create(command));
    }

    @Operation(summary = "修改参数配置")
    @Log(title = "参数管理", businessType = BusinessTypeEnum.UPDATE)
    @SaCheckPermission("system:config:edit")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody SaveConfigCommand command) {
        configCommandService.update(id, command);
        return Result.success();
    }

    @Operation(summary = "删除参数配置")
    @Log(title = "参数管理", businessType = BusinessTypeEnum.DELETE)
    @SaCheckPermission("system:config:remove")
    @DeleteMapping("/{ids}")
    public Result<Void> delete(@PathVariable Long[] ids) {
        configCommandService.delete(ids);
        return Result.success();
    }

    @Operation(summary = "刷新参数配置缓存")
    @Log(title = "参数管理", businessType = BusinessTypeEnum.OTHER)
    @SaCheckPermission("system:config:list")
    @DeleteMapping("/refreshCache")
    public Result<Void> refreshCache() {
        configCommandService.refreshCache();
        return Result.success();
    }
}
