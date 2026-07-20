package com.silverwing.auth.application.command;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录命令
 */
@Data
public class LoginCommand {

    @NotBlank(message = "{validation.login.username.notblank}")
    private String username;

    @NotBlank(message = "{validation.login.password.notblank}")
    private String password;
}
