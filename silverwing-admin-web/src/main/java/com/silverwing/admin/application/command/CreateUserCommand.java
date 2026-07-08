package com.silverwing.admin.application.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建用户命令
 */
@Data
public class CreateUserCommand {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 30, message = "用户名长度需在3-30之间")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 50, message = "密码长度需在6-50之间")
    private String password;

    private Integer sex;
    private String avatar;
    private String phone;
    private String email;
}
