package com.silverwing.auth.application.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 验证码配置项。
 * <p>对应 Nacos / application.yml 中的 {@code security.captcha} 配置节点。</p>
 */
@Component
@ConfigurationProperties(prefix = "security.captcha")
public class CaptchaProperties {

    /**
     * 是否开启验证码校验，默认开启
     */
    private boolean enabled = true;

    /**
     * 验证码类型：math（算术）或 char（字符），默认算术
     */
    private String type = "math";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
