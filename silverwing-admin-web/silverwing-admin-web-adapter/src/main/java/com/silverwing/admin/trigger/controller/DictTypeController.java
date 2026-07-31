package com.silverwing.admin.trigger.controller;

import com.silverwing.admin.application.command.DictTypeCommandService;
import com.silverwing.admin.application.command.SaveDictTypeCommand;
import com.silverwing.admin.application.dto.DictTypeResponse;
import com.silverwing.admin.application.query.DictTypePageQuery;
import com.silverwing.admin.application.query.DictTypeQueryService;
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
 * 字典类型管理。
 */
@RestController
@RequestMapping("/system/dict/type")
@Tag(name = "字典类型管理")
@RequiredArgsConstructor
public class DictTypeController {

    private final DictTypeQueryService dictTypeQueryService;
    private final DictTypeCommandService dictTypeCommandService;

    @Operation(summary = "字典类型分页列表")
    @SaCheckPermission("system:dict:list")
    @GetMapping("/list")
    public Result<PageResult<DictTypeResponse>> list(DictTypePageQuery query) {
        return Result.success(dictTypeQueryService.list(query));
    }

    @Operation(summary = "导出字典类型")
    @Log(title = "字典类型", businessType = BusinessTypeEnum.OTHER)
    @SaCheckPermission("system:dict:export")
    @PostMapping("/export")
    public void export(HttpServletResponse response, DictTypePageQuery query) throws IOException {
        List<DictTypeResponse> list = dictTypeQueryService.listExport(query);
        ExcelUtils.export(response, list, DictTypeResponse.class, "admin.dict.type.export.name");
    }

    @Operation(summary = "字典类型详情")
    @SaCheckPermission("system:dict:query")
    @GetMapping("/{id}")
    public Result<DictTypeResponse> getById(@PathVariable Long id) {
        return Result.success(dictTypeQueryService.getById(id));
    }

    @Operation(summary = "新增字典类型")
    @Log(title = "字典类型", businessType = BusinessTypeEnum.INSERT)
    @SaCheckPermission("system:dict:add")
    @PostMapping
    public Result<DictTypeResponse> create(@Valid @RequestBody SaveDictTypeCommand command) {
        return Result.success(dictTypeCommandService.create(command));
    }

    @Operation(summary = "修改字典类型")
    @Log(title = "字典类型", businessType = BusinessTypeEnum.UPDATE)
    @SaCheckPermission("system:dict:edit")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody SaveDictTypeCommand command) {
        dictTypeCommandService.update(id, command);
        return Result.success();
    }

    @Operation(summary = "删除字典类型")
    @Log(title = "字典类型", businessType = BusinessTypeEnum.DELETE)
    @SaCheckPermission("system:dict:remove")
    @DeleteMapping("/{ids}")
    public Result<Void> delete(@PathVariable Long[] ids) {
        dictTypeCommandService.delete(ids);
        return Result.success();
    }

    @Operation(summary = "刷新字典缓存")
    @Log(title = "字典类型", businessType = BusinessTypeEnum.OTHER)
    @SaCheckPermission("system:dict:remove")
    @DeleteMapping("/refreshCache")
    public Result<Void> refreshCache() {
        dictTypeQueryService.refreshCache();
        return Result.success();
    }

    @Operation(summary = "字典类型下拉选项")
    @SaCheckPermission("system:dict:query")
    @GetMapping("/optionSelect")
    public Result<List<DictTypeResponse>> optionSelect() {
        return Result.success(dictTypeQueryService.optionSelect());
    }
}
