package com.silverwing.admin.trigger.controller;

import com.silverwing.admin.application.command.NoticeCommandService;
import com.silverwing.admin.application.command.SaveNoticeCommand;
import com.silverwing.admin.application.dto.NoticeReadUserResponse;
import com.silverwing.admin.application.dto.NoticeResponse;
import com.silverwing.admin.application.query.NoticePageQuery;
import com.silverwing.admin.application.query.NoticeQueryService;
import com.silverwing.admin.application.query.NoticeReadUserPageQuery;
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
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

/**
 * 通知公告管理。
 */
@RestController
@RequestMapping("/notice")
@Tag(name = "通知公告管理")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeQueryService noticeQueryService;
    private final NoticeCommandService noticeCommandService;

    @Operation(summary = "通知公告分页列表")
    @SaCheckPermission("system:notice:list")
    @GetMapping("/list")
    public Result<PageResult<NoticeResponse>> list(NoticePageQuery query) {
        return Result.success(noticeQueryService.list(query));
    }

    @Operation(summary = "导出通知公告")
    @Log(title = "通知公告", businessType = BusinessTypeEnum.OTHER)
    @SaCheckPermission("system:notice:export")
    @PostMapping("/export")
    public void export(HttpServletResponse response, NoticePageQuery query) throws IOException {
        List<NoticeResponse> list = noticeQueryService.listExport(query);
        ExcelUtils.export(response, list, NoticeResponse.class, "admin.notice.export.name");
    }

    @Operation(summary = "通知公告详情")
    @SaCheckPermission("system:notice:query")
    @GetMapping("/{noticeId}")
    public Result<NoticeResponse> getById(@PathVariable("noticeId") Long noticeId) {
        return Result.success(noticeQueryService.getById(noticeId));
    }

    @Operation(summary = "新增通知公告")
    @Log(title = "通知公告", businessType = BusinessTypeEnum.INSERT)
    @SaCheckPermission("system:notice:add")
    @PostMapping
    public Result<NoticeResponse> create(@Valid @RequestBody SaveNoticeCommand command) {
        return Result.success(noticeCommandService.create(command));
    }

    @Operation(summary = "修改通知公告")
    @Log(title = "通知公告", businessType = BusinessTypeEnum.UPDATE)
    @SaCheckPermission("system:notice:edit")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody SaveNoticeCommand command) {
        noticeCommandService.update(id, command);
        return Result.success();
    }

    @Operation(summary = "删除通知公告")
    @Log(title = "通知公告", businessType = BusinessTypeEnum.DELETE)
    @SaCheckPermission("system:notice:remove")
    @DeleteMapping("/{ids}")
    public Result<Void> delete(@PathVariable Long[] ids) {
        noticeCommandService.delete(ids);
        return Result.success();
    }

    @Operation(summary = "标记公告已读")
    @PostMapping("/markRead/{noticeId}")
    public Result<Void> markRead(@PathVariable Long noticeId) {
        // 已读行为归属当前登录用户，用户ID从会话获取，不接受前端传参
        noticeCommandService.markRead(noticeId, StpUtil.getLoginIdAsLong());
        return Result.success();
    }

    @Operation(summary = "批量标记公告已读")
    @PostMapping("/markReadAll")
    public Result<Void> markReadAll(@RequestParam Long[] noticeIds) {
        noticeCommandService.markReadBatch(StpUtil.getLoginIdAsLong(), noticeIds);
        return Result.success();
    }

    @Operation(summary = "公告已读用户列表")
    @SaCheckPermission("system:notice:list")
    @GetMapping("/readUsers/list")
    public Result<PageResult<NoticeReadUserResponse>> readUsers(NoticeReadUserPageQuery query) {
        return Result.success(noticeQueryService.readUsers(query));
    }
}
