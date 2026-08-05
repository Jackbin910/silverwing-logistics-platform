package com.silverwing.admin.trigger.controller;

import com.silverwing.admin.application.command.LogininforCommandService;
import com.silverwing.admin.application.dto.LogininforResponse;
import com.silverwing.admin.application.query.LogininforPageQuery;
import com.silverwing.admin.application.query.LogininforQueryService;
import com.silverwing.admin.trigger.dto.LogininforExportVO;
import com.silverwing.common.annotation.Log;
import com.silverwing.common.domain.PageResult;
import com.silverwing.common.domain.Result;
import com.silverwing.common.enums.BusinessTypeEnum;
import com.silverwing.common.util.ExcelUtils;
import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 系统访问记录管理控制器。
 * <p>提供访问记录的查询、导出、删除与清空能力，对应 RuoYi 的 SysLogininforController。</p>
 *
 * @author silverwing
 */
@Tag(name = "登录日志", description = "系统访问记录查询、导出、删除与清空")
@RestController
@RequestMapping("/monitor/logininfor")
@RequiredArgsConstructor
public class LogininforController {

    private final LogininforQueryService logininforQueryService;

    private final LogininforCommandService logininforCommandService;

    @Operation(summary = "查询登录日志列表")
    @SaCheckPermission("monitor:logininfor:list")
    @GetMapping("/list")
    public Result<PageResult<LogininforResponse>> list(LogininforPageQuery query) {
        return Result.success(logininforQueryService.list(query));
    }

    @Operation(summary = "导出登录日志")
    @SaCheckPermission("monitor:logininfor:export")
    @Log(title = "登录日志", businessType = BusinessTypeEnum.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, LogininforPageQuery query) {
        List<LogininforResponse> list = logininforQueryService.listExport(query);
        List<LogininforExportVO> data = list.stream().map(this::toExportVo).toList();
        ExcelUtils.export(response, data, LogininforExportVO.class, "admin.logininfor.export.name");
    }

    /**
     * 将访问记录响应对象转换为导出视图对象。
     *
     * @param source 访问记录响应对象
     * @return 访问记录导出视图对象
     */
    private LogininforExportVO toExportVo(LogininforResponse source) {
        LogininforExportVO vo = new LogininforExportVO();
        vo.setInfoId(source.getInfoId());
        vo.setUserName(source.getUserName());
        vo.setIpaddr(source.getIpaddr());
        vo.setStatus(source.getStatus());
        vo.setMsg(source.getMsg());
        vo.setAccessTime(source.getAccessTime());
        return vo;
    }

    @Operation(summary = "删除登录日志")
    @SaCheckPermission("monitor:logininfor:remove")
    @Log(title = "登录日志", businessType = BusinessTypeEnum.DELETE)
    @DeleteMapping("/{infoIds}")
    public Result<Void> remove(@PathVariable List<Long> infoIds) {
        logininforCommandService.removeByIds(infoIds.toArray(new Long[0]));
        return Result.success();
    }

    @Operation(summary = "清空登录日志")
    @SaCheckPermission("monitor:logininfor:remove")
    @Log(title = "登录日志", businessType = BusinessTypeEnum.DELETE)
    @DeleteMapping("/clean")
    public Result<Void> clean() {
        logininforCommandService.clean();
        return Result.success();
    }

    @Operation(summary = "解锁账号")
    @SaCheckPermission("monitor:logininfor:unlock")
    @Log(title = "登录日志", businessType = BusinessTypeEnum.OTHER)
    @GetMapping("/unlock/{userName}")
    public Result<Void> unlock(@PathVariable("userName") String userName) {
        logininforCommandService.unlock(userName);
        return Result.success();
    }
}
