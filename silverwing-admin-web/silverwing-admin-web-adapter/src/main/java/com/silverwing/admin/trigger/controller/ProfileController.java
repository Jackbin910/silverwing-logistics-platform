package com.silverwing.admin.trigger.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.IdUtil;
import com.silverwing.admin.application.command.ProfileCommandService;
import com.silverwing.admin.application.command.UpdateProfileCommand;
import com.silverwing.admin.application.command.UpdateProfilePasswordCommand;
import com.silverwing.admin.application.dto.ProfileResponse;
import com.silverwing.admin.application.query.ProfileQueryService;
import com.silverwing.common.annotation.Log;
import com.silverwing.common.domain.Result;
import com.silverwing.common.domain.ResultCode;
import com.silverwing.common.enums.BusinessTypeEnum;
import com.silverwing.common.exception.BusinessException;
import com.silverwing.common.storage.core.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

/**
 * 个人中心控制器（迁移自 RuoYi-Cloud SysProfileController）
 * <p>提供当前登录用户的资料读取与修改、密码修改、头像上传能力。当前登录用户 ID
 * 通过 Sa-Token 上下文获取，而非 URL 入参，避免越权操作他人资料。</p>
 *
 * @author silverwing
 */
@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "个人中心")
public class ProfileController {

    /**
     * 允许上传的头像扩展名
     */
    private static final Set<String> ALLOWED_AVATAR_EXT = Set.of("png", "jpg", "jpeg", "gif");

    private final ProfileQueryService profileQueryService;

    private final ProfileCommandService profileCommandService;

    private final FileStorageService fileStorageService;

    /**
     * 获取当前登录用户个人资料（含角色组）
     */
    @SaCheckPermission("system:user:query")
    @Operation(summary = "获取当前登录用户个人资料")
    @GetMapping("/profile")
    public Result<ProfileResponse> profile() {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(profileQueryService.getProfile(userId));
    }

    /**
     * 修改个人资料（昵称、邮箱、手机号、性别）
     */
    @Log(title = "个人中心-修改资料", businessType = BusinessTypeEnum.UPDATE)
    @SaCheckPermission("system:user:edit")
    @Operation(summary = "修改个人资料")
    @PutMapping("/profile")
    public Result<Void> updateProfile(@Validated @RequestBody UpdateProfileCommand command) {
        Long userId = StpUtil.getLoginIdAsLong();
        profileCommandService.updateProfile(userId, command);
        return Result.success();
    }

    /**
     * 修改密码（需校验旧密码且新密码不能与旧密码相同）
     */
    @Log(title = "个人中心-修改密码", businessType = BusinessTypeEnum.UPDATE)
    @SaCheckPermission("system:user:edit")
    @Operation(summary = "修改密码")
    @PutMapping("/profile/updatePwd")
    public Result<Void> updatePwd(@Validated @RequestBody UpdateProfilePasswordCommand command) {
        Long userId = StpUtil.getLoginIdAsLong();
        profileCommandService.updateSelfPassword(userId, command);
        return Result.success();
    }

    /**
     * 上传并更新头像
     *
     * @param file 头像文件（param 名称 avatarfile）
     * @return 头像访问地址
     */
    @Log(title = "个人中心-修改头像", businessType = BusinessTypeEnum.UPDATE)
    @SaCheckPermission("system:user:edit")
    @Operation(summary = "上传头像")
    @PostMapping("/profile/avatar")
    public Result<String> avatar(@RequestParam("avatarfile") MultipartFile file) {
        Long userId = StpUtil.getLoginIdAsLong();
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "admin.profile.avatar.empty");
        }
        String ext = getExtension(file.getOriginalFilename());
        if (!ALLOWED_AVATAR_EXT.contains(ext)) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "admin.profile.avatar.type.error");
        }
        String key = "avatar/" + userId + "_" + IdUtil.fastSimpleUUID() + "." + ext;
        try {
            String uploadedKey = fileStorageService.upload(file, key);
            String url = fileStorageService.getFileUrl(uploadedKey);
            profileCommandService.updateAvatar(userId, url);
            return Result.success(url);
        } catch (RuntimeException e) {
            log.error("头像上传失败 [userId={}]：{}", userId, e.getMessage(), e);
            throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR,
                    "admin.profile.avatar.upload.failed", e.getMessage());
        }
    }

    /**
     * 提取文件名扩展名（不含点）
     *
     * @param filename 原始文件名
     * @return 小写扩展名，无扩展名返回空串
     */
    private static String getExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') < 0) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}
