package com.silverwing.admin.trigger.controller;

import com.silverwing.admin.application.command.DictDataCommandService;
import com.silverwing.admin.application.command.SaveDictDataCommand;
import com.silverwing.admin.application.dto.DictDataResponse;
import com.silverwing.admin.application.query.DictDataPageQuery;
import com.silverwing.admin.application.query.DictDataQueryService;
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
 * 字典数据管理。
 */
@RestController
@RequestMapping("/dict/data")
@Tag(name = "字典数据管理")
@RequiredArgsConstructor
public class DictDataController {

    private final DictDataQueryService dictDataQueryService;
    private final DictDataCommandService dictDataCommandService;

    @Operation(summary = "字典数据分页列表")
    @SaCheckPermission("system:dict:list")
    @GetMapping("/list")
    public Result<PageResult<DictDataResponse>> list(DictDataPageQuery query) {
        return Result.success(dictDataQueryService.list(query));
    }

    @Operation(summary = "导出字典数据")
    @Log(title = "字典数据", businessType = BusinessTypeEnum.OTHER)
    @SaCheckPermission("system:dict:export")
    @PostMapping("/export")
    public void export(HttpServletResponse response, DictDataPageQuery query) throws IOException {
        List<DictDataResponse> list = dictDataQueryService.listExport(query);
        ExcelUtils.export(response, list, DictDataResponse.class, "admin.dict.data.export.name");
    }

    @Operation(summary = "字典数据详情")
    @SaCheckPermission("system:dict:query")
    @GetMapping("/{id}")
    public Result<DictDataResponse> getById(@PathVariable Long id) {
        return Result.success(dictDataQueryService.getById(id));
    }

    @Operation(summary = "按字典类型查询字典数据")
    @SaCheckPermission("system:dict:query")
    @GetMapping("/type/{dictType}")
    public Result<List<DictDataResponse>> getByDictType(@PathVariable String dictType) {
        return Result.success(dictDataQueryService.getByDictType(dictType));
    }

    @Operation(summary = "新增字典数据")
    @Log(title = "字典数据", businessType = BusinessTypeEnum.INSERT)
    @SaCheckPermission("system:dict:add")
    @PostMapping
    public Result<DictDataResponse> create(@Valid @RequestBody SaveDictDataCommand command) {
        return Result.success(dictDataCommandService.create(command));
    }

    @Operation(summary = "修改字典数据")
    @Log(title = "字典数据", businessType = BusinessTypeEnum.UPDATE)
    @SaCheckPermission("system:dict:edit")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody SaveDictDataCommand command) {
        dictDataCommandService.update(id, command);
        return Result.success();
    }

    @Operation(summary = "删除字典数据")
    @Log(title = "字典数据", businessType = BusinessTypeEnum.DELETE)
    @SaCheckPermission("system:dict:remove")
    @DeleteMapping("/{ids}")
    public Result<Void> delete(@PathVariable Long[] ids) {
            dictDataCommandService.delete(ids);
        return Result.success();
    }
}
