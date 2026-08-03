package com.silverwing.auth.infrastructure.config;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * 验证码生成配置。
 * <p>
 * 提供两种 Producer：字符验证码（{@code captchaProducer}）与算术验证码（{@code captchaProducerMath}），
 * 与 RuoYi 风格保持一致；由 application 层的 {@code CaptchaService} 按 {@code CaptchaProperties.type} 选择使用。
 * </p>
 */
@Configuration
public class CaptchaConfig {

    /**
     * 字符验证码 Producer。
     */
    @Bean(name = "captchaProducer")
    public DefaultKaptcha captchaProducer() {
        DefaultKaptcha defaultKaptcha = new DefaultKaptcha();
        Properties properties = new Properties();
        // 是否有边框
        properties.setProperty("kaptcha.border", "no");
        // 边框颜色
        properties.setProperty("kaptcha.border.color", "105,179,90");
        // 验证码文本字符颜色
        properties.setProperty("kaptcha.textproducer.font.color", "blue");
        // 验证码图片宽度
        properties.setProperty("kaptcha.image.width", "125");
        // 验证码图片高度
        properties.setProperty("kaptcha.image.height", "45");
        // 验证码文本字符大小
        properties.setProperty("kaptcha.textproducer.font.size", "35");
        // 验证码过期时间（分钟），此处仅标识用途，实际存储过期由仓储层控制
        properties.setProperty("kaptcha.textproducer.char.space", "10");
        // 验证码文本字符长度
        properties.setProperty("kaptcha.textproducer.char.length", "4");
        // 验证码文本字体样式
        properties.setProperty("kaptcha.textproducer.font.names", "Arial,Courier");
        // 验证码文本字符内容范围（数字 + 字母）
        properties.setProperty("kaptcha.textproducer.char.string", "123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        // 干扰线颜色
        properties.setProperty("kaptcha.noise.color", "blue");
        defaultKaptcha.setConfig(new Config(properties));
        return defaultKaptcha;
    }

    /**
     * 算术验证码 Producer。
     * <p>生成格式如 {@code "1+2=?@3"}，@ 前为展示给用户的算式，@ 后为正确答案。</p>
     */
    @Bean(name = "captchaProducerMath")
    public DefaultKaptcha captchaProducerMath() {
        DefaultKaptcha defaultKaptcha = new DefaultKaptcha();
        Properties properties = new Properties();
        properties.setProperty("kaptcha.border", "no");
        properties.setProperty("kaptcha.border.color", "105,179,90");
        properties.setProperty("kaptcha.textproducer.font.color", "blue");
        properties.setProperty("kaptcha.image.width", "125");
        properties.setProperty("kaptcha.image.height", "45");
        properties.setProperty("kaptcha.textproducer.font.size", "35");
        properties.setProperty("kaptcha.textproducer.char.space", "10");
        properties.setProperty("kaptcha.textproducer.font.names", "Arial,Courier");
        // 算术验证码文本生成器：生成 "a+b=?@c" 格式
        properties.setProperty("kaptcha.textproducer.impl",
                "com.silverwing.auth.infrastructure.config.MathCaptchaTextCreator");
        properties.setProperty("kaptcha.noise.color", "blue");
        defaultKaptcha.setConfig(new Config(properties));
        return defaultKaptcha;
    }
}
