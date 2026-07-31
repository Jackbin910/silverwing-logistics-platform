package com.silverwing.admin.trigger.controller;

import com.silverwing.admin.application.command.PostCommandService;
import com.silverwing.admin.application.command.SavePostCommand;
import com.silverwing.admin.application.dto.PostExportVO;
import com.silverwing.admin.application.dto.PostResponse;
import com.silverwing.admin.application.query.PostPageQuery;
import com.silverwing.admin.application.query.PostQueryService;
import com.silverwing.common.annotation.Log;
import com.silverwing.common.domain.PageResult;
import com.silverwing.common.domain.Result;
import com.silverwing.common.enums.BusinessTypeEnum;
import com.silverwing.common.util.ExcelUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 岗位管理控制器（迁移自 RuoYi-Cloud SysPostController）
 *
 * @author silverwing
 */
@Slf4j
@RestController
@RequestMapping("/post")
@RequiredArgsConstructor
@Tag(name = "岗位管理")
public class PostController {

    private final PostQueryService postQueryService;
    private final PostCommandService postCommandService;

    /**
     * 分页查询岗位列表
     */
    @SaCheckPermission("system:post:list")
    @Operation(summary = "分页查询岗位列表")
    @GetMapping("/list")
    public Result<PageResult<PostResponse>> list(PostPageQuery query) {
        return Result.success(postQueryService.list(query));
    }

    /**
     * 导出岗位数据
     */
    @Log(title = "岗位管理-导出", businessType = BusinessTypeEnum.EXPORT)
    @Operation(summary = "导出岗位数据")
    @PostMapping("/export")
    @SaCheckPermission("system:post:export")
    public void export(HttpServletResponse response) {
        List<PostResponse> list = postQueryService.exportList();
        List<PostExportVO> data = list.stream().map(this::toExportVo).toList();
        ExcelUtils.export(response, data, PostExportVO.class, "admin.post.export.name");
    }

    /**
     * 查询岗位选项（正常状态）
     */
    @Operation(summary = "查询岗位选项")
    @GetMapping("/optionselect")
    public Result<List<PostResponse>> optionSelect() {
        return Result.success(postQueryService.optionSelect());
    }

    /**
     * 根据岗位ID查询详情
     */
    @Operation(summary = "根据岗位ID查询详情")
    @GetMapping("/{postId}")
    @SaCheckPermission("system:post:query")
    public Result<PostResponse> getInfo(@PathVariable Long postId) {
        return Result.success(postQueryService.getById(postId));
    }

    /**
     * 新增岗位
     */
    @Log(title = "岗位管理-新增", businessType = BusinessTypeEnum.INSERT)
    @Operation(summary = "新增岗位")
    @PostMapping
    @SaCheckPermission("system:post:add")
    public Result<PostResponse> add(@Valid @RequestBody SavePostCommand command) {
        return Result.success(postCommandService.create(command));
    }

    /**
     * 修改岗位
     */
    @Log(title = "岗位管理-修改", businessType = BusinessTypeEnum.UPDATE)
    @Operation(summary = "修改岗位")
    @PutMapping
    @SaCheckPermission("system:post:edit")
    public Result<Void> edit(@Valid @RequestBody SavePostCommand command) {
        if (command.getId() == null) {
            return Result.fail(com.silverwing.common.domain.ResultCode.BUSINESS_ERROR, "admin.post.id.required");
        }
        postCommandService.update(command.getId(), command);
        return Result.success();
    }

    /**
     * 删除岗位
     */
    @Log(title = "岗位管理-删除", businessType = BusinessTypeEnum.DELETE)
    @Operation(summary = "删除岗位")
    @DeleteMapping("/{postIds}")
    @SaCheckPermission("system:post:remove")
    public Result<Void> remove(@PathVariable List<Long> postIds) {
        postCommandService.delete(postIds);
        return Result.success();
    }

    private PostExportVO toExportVo(PostResponse post) {
        PostExportVO vo = new PostExportVO();
        vo.setId(post.getId());
        vo.setPostCode(post.getPostCode());
        vo.setPostName(post.getPostName());
        vo.setPostSort(post.getPostSort());
        vo.setStatus(post.getStatus());
        vo.setUserCount(post.getUserCount());
        vo.setCreateTime(post.getCreateTime());
        return vo;
    }
}
