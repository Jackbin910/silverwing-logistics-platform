package com.silverwing.admin.trigger.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.silverwing.admin.application.dto.UserOnlineVO;
import com.silverwing.admin.application.query.UserOnlineQueryService;
import com.silverwing.common.domain.PageResult;
import com.silverwing.common.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 在线用户监控接口（薄控制器，仅做 HTTP 转换与路由）
 * <p>查询与强退均委托 application/query 的 {@link UserOnlineQueryService}。</p>
 *
 * @author silverwing
 */
@Slf4j
@RestController
@Tag(name = "在线用户监控", description = "在线用户查询与强制下线")
@RequestMapping("/monitor/online")
@RequiredArgsConstructor
public class UserOnlineController {

    private final UserOnlineQueryService userOnlineQueryService;

    @SaCheckPermission("monitor:online:list")
    @Operation(summary = "查询在线用户列表")
    @GetMapping("/list")
    public Result<PageResult<UserOnlineVO>> list(@RequestParam(required = false) String ipaddr,
                                                  @RequestParam(required = false) String userName,
                                                  @RequestParam(defaultValue = "1") Long current,
                                                  @RequestParam(defaultValue = "10") Long size) {
        return Result.success(userOnlineQueryService.list(ipaddr, userName, current, size));
    }

    @SaCheckPermission("monitor:online:forceLogout")
    @Operation(summary = "强制用户下线")
    @DeleteMapping("/{tokenId}")
    public Result<Void> forceLogout(@PathVariable("tokenId") String tokenId) {
        userOnlineQueryService.forceLogout(tokenId);
        return Result.success("强制下线成功");
    }
}
