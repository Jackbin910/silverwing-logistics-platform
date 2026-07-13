package com.silverwing.auth.config;

import cn.hutool.crypto.asymmetric.RSA;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.KeyPair;

/**
 * RSA 密钥配置
 * <p>
 * 用于登录密码的传输加密：前端用公钥加密明文密码，后端用私钥解密。
 * 解密后的明文密码继续走 {@link com.silverwing.biz.iam.domain.model.aggregate.SysUserAggregate#matchesPassword}
 * 的 MD5(salt + 明文) 校验流程，不改变现有存储与比对方式。
 * </p>
 * <p>
 * 密钥来源（二选一）：
 * <ol>
 *   <li>配置文件指定：{@code silverwing.security.rsa.public-key} / {@code silverwing.security.rsa.private-key}（多实例部署必填）</li>
 *   <li>未配置时启动自动生成（单实例开发环境适用，重启后公钥变化）</li>
 * </ol>
 * </p>
 *
 * @author silverwing
 */
@Slf4j
@Component
public class RsaKeyConfig {

    /**
     * 可配置的 RSA 公钥（Base64），多实例部署时需统一配置
     */
    @Value("${silverwing.security.rsa.public-key:}")
    private String configuredPublicKey;

    /**
     * 可配置的 RSA 私钥（Base64），多实例部署时需统一配置
     */
    @Value("${silverwing.security.rsa.private-key:}")
    private String configuredPrivateKey;

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
     * 初始化 RSA 密钥对：优先使用配置的密钥，未配置则自动生成。
     */
    @PostConstruct
    public void init() {
        if (configuredPublicKey != null && !configuredPublicKey.isBlank()
                && configuredPrivateKey != null && !configuredPrivateKey.isBlank()) {
            // 使用配置的密钥对（多实例部署）
            rsa = new RSA(configuredPrivateKey, configuredPublicKey);
            log.info("RSA 密钥对已从配置加载");
        } else {
            // 自动生成密钥对（单实例开发环境）
            rsa = new RSA();
            log.info("RSA 密钥对已自动生成（重启后公钥将变化，生产环境请配置固定密钥）");
        }
        publicKeyBase64 = rsa.getPublicKeyBase64();
    }
}
