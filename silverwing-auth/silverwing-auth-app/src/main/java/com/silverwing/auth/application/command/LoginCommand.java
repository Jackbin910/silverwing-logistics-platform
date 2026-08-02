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

    /**
     * 登录 IP（由网关/控制器解析后填充，用于在线用户监控）
     */
    private String ipaddr;

    /**
     * 登录浏览器（由 User-Agent 解析后填充）
     */
    private String browser;

    /**
     * 登录操作系统（由 User-Agent 解析后填充）
     */
    private String os;

    /**
     * 验证码唯一标识（前端从 /auth/captcha 获取）
     */
    private String uuid;

    /**
     * 用户输入的验证码答案
     */
    private String code;
}
