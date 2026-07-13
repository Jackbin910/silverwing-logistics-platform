package com.silverwing.auth.config;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.crypto.asymmetric.RSA;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * RSA 密钥配置
 * <p>
 * 用于登录密码的传输加密：前端用公钥加密明文密码，后端用私钥解密。
 * 解密后的明文密码继续走 BCrypt 校验流程。
 * </p>
 * <p>
 * 密钥必须通过配置文件提供，不自动生成：
 * <pre>
 * silverwing:
 *   security:
 *     rsa:
 *       public-key: "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8A..."
 *       private-key: "MIIEvQIBADANBgkqhkiG9w0BAQEF..."
 * </pre>
 * 配置通常放在 Nacos 的 auth 服务配置文件中。
 * </p>
 *
 * @author silverwing
 */
@Slf4j
@Component
@Setter
@ConfigurationProperties(prefix = "silverwing.security.rsa")
public class RsaKeyConfig {

    /**
     * RSA 公钥（Base64），前端加密用
     */
    private String publicKey;

    /**
     * RSA 私钥（Base64），后端解密用
     */
    private String privateKey;

    /**
     * RSA 实例，持有密钥对
     */
    @Getter
    private RSA rsa;

    /**
     * 公钥（Base64 字符串），供前端加密使用
     */
    @Getter
    private String publicKeyBase64;

    /**
     * 从配置加载 RSA 密钥对，密钥未配置时启动直接失败。
     */
    @PostConstruct
    public void init() {
        if (CharSequenceUtil.isBlank(publicKey)
                || CharSequenceUtil.isBlank(privateKey)) {
            throw new IllegalStateException(
                    "RSA 密钥未配置，请在配置文件中设置 silverwing.security.rsa.public-key 和 silverwing.security.rsa.private-key");
        }
        rsa = new RSA(privateKey, publicKey);
        publicKeyBase64 = rsa.getPublicKeyBase64();
        log.info("RSA 密钥对已从配置加载");
    }

}
