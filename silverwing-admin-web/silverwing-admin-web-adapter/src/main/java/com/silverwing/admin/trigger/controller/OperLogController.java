package com.silverwing.admin.trigger.controller;

import com.silverwing.admin.application.command.OperLogCommandService;
import com.silverwing.admin.application.dto.OperLogResponse;
import com.silverwing.admin.trigger.dto.OperLogExportVO;
import com.silverwing.admin.application.query.OperLogPageQuery;
import com.silverwing.admin.application.query.OperLogQueryService;
import com.silverwing.common.annotation.Log;
import com.silverwing.common.domain.Result;
import com.silverwing.common.domain.PageResult;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 操作日志记录管理控制器。
 */
@Tag(name = "操作日志", description = "操作日志记录查询、导出、删除与清空")
@RestController
@RequestMapping("/monitor/operlog")
@RequiredArgsConstructor
public class OperLogController {

    private final OperLogQueryService operLogQueryService;
    private final OperLogCommandService operLogCommandService;

    @Operation(summary = "查询操作日志列表")
    @SaCheckPermission("monitor:operlog:list")
    @GetMapping("/list")
    public Result<PageResult<OperLogResponse>> list(OperLogPageQuery query) {
        return Result.success(operLogQueryService.list(query));
    }

    @Operation(summary = "导出操作日志")
    @SaCheckPermission("monitor:operlog:export")
    @Log(title = "操作日志", businessType = BusinessTypeEnum.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, OperLogPageQuery query) {
        List<OperLogResponse> list = operLogQueryService.listExport(query);
        List<OperLogExportVO> data = list.stream().map(this::toExportVo).toList();
        ExcelUtils.export(response, data, OperLogExportVO.class, "admin.oper.log.export.name");
    }

    /**
     * 将操作日志响应对象转换为导出视图对象。
     *
     * @param source 操作日志响应对象
     * @return 操作日志导出视图对象
     */
    private OperLogExportVO toExportVo(OperLogResponse source) {
        OperLogExportVO vo = new OperLogExportVO();
        vo.setOperId(source.getOperId());
        vo.setTitle(source.getTitle());
        vo.setBusinessType(source.getBusinessType());
        vo.setOperName(source.getOperName());
        vo.setDeptName(source.getDeptName());
        vo.setOperUrl(source.getOperUrl());
        vo.setOperIp(source.getOperIp());
        vo.setOperLocation(source.getOperLocation());
        vo.setRequestMethod(source.getRequestMethod());
        vo.setMethod(source.getMethod());
        vo.setOperParam(source.getOperParam());
        vo.setJsonResult(source.getJsonResult());
        vo.setStatus(source.getStatus());
        vo.setErrorMsg(source.getErrorMsg());
        vo.setOperTime(source.getOperTime());
        vo.setCostTime(source.getCostTime());
        return vo;
    }

    @Operation(summary = "根据ID查询操作日志明细")
    @SaCheckPermission("monitor:operlog:query")
    @GetMapping("/{operId}")
    public Result<OperLogResponse> getInfo(@PathVariable Long operId) {
        return Result.success(operLogQueryService.getById(operId));
    }

    @Operation(summary = "删除操作日志")
    @SaCheckPermission("monitor:operlog:remove")
    @Log(title = "操作日志", businessType = BusinessTypeEnum.DELETE)
    @DeleteMapping("/{operIds}")
    public Result<Void> remove(@PathVariable List<Long> operIds) {
        operLogCommandService.removeByIds(operIds);
        return Result.success();
    }

    @Operation(summary = "清空操作日志")
    @SaCheckPermission("monitor:operlog:remove")
    @Log(title = "操作日志", businessType = BusinessTypeEnum.DELETE)
    @DeleteMapping("/clean")
    public Result<Void> clean() {
        operLogCommandService.clean();
        return Result.success();
    }
}
