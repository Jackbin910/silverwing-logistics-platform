package com.silverwing.admin.application.command;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 修改密码命令。
 *
 * @author silverwing
 */
@Data
public class UpdateProfilePasswordCommand {

    /**
     * 旧密码
     */
    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;

    /**
     * 新密码
     */
    @NotBlank(message = "新密码不能为空")
    private String newPassword;
}
