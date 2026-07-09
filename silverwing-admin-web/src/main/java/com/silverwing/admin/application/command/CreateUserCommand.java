package com.silverwing.admin.application.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建用户命令
 */
@Data
public class CreateUserCommand {

    @NotBlank(message = "{validation.user.username.notblank}")
    @Size(min = 3, max = 30, message = "{validation.user.username.size}")
    private String username;

    @NotBlank(message = "{validation.user.password.notblank}")
    @Size(min = 6, max = 50, message = "{validation.user.password.size}")
    private String password;

    private Integer sex;
    private String avatar;
    private String phone;
    private String email;
}
