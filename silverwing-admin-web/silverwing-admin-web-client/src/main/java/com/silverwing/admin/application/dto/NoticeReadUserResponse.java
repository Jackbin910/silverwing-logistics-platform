package com.silverwing.admin.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 公告已读用户响应。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "公告已读用户响应")
public class NoticeReadUserResponse {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户账号")
    private String username;

    @Schema(description = "用户昵称")
    private String nickname;

    @Schema(description = "部门名称")
    private String deptName;

    @Schema(description = "手机号码")
    private String phone;

    @Schema(description = "阅读时间")
    private LocalDateTime readTime;
}
