package com.silverwing.admin.application.command;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 修改个人资料命令。
 * <p>仅允许修改昵称、邮箱、手机号、性别，密码等敏感字段由独立接口处理。</p>
 *
 * @author silverwing
 */
@Data
public class UpdateProfileCommand {

    /**
     * 用户昵称
     */
    @Size(max = 50, message = "用户昵称长度不能超过 50 个字符")
    private String nickname;

    /**
     * 邮箱
     */
    @Email(message = "邮箱格式不正确")
    @Size(max = 50, message = "邮箱长度不能超过 50 个字符")
    private String email;

    /**
     * 手机号
     */
    @Size(max = 11, message = "手机号长度不能超过 11 个字符")
    private String phone;

    /**
     * 性别（0 男 1 女 2 未知）
     */
    private Integer sex;
}
