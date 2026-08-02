package com.silverwing.auth.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 验证码响应视图对象。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaptchaVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 验证码唯一标识，登录时需回传
     */
    private String uuid;

    /**
     * 验证码图片（Base64，形如 data:image/png;base64,…）
     */
    private String img;

    /**
     * 是否开启验证码校验
     */
    private Boolean enabled;
}
