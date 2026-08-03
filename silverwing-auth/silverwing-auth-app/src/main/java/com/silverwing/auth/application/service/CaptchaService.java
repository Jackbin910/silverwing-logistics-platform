package com.silverwing.auth.application.service;

import com.google.code.kaptcha.Producer;
import com.silverwing.auth.application.config.CaptchaProperties;
import com.silverwing.auth.application.dto.CaptchaVO;
import com.silverwing.auth.iam.domain.adapter.repository.CaptchaRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.UUID;

/**
 * 验证码应用服务。
 * <p>负责生成验证码图片与答案、将答案存储到 Redis，并提供校验能力。</p>
 */
@Service
public class CaptchaService {

    private final CaptchaProperties captchaProperties;
    private final CaptchaRepository captchaRepository;
    private final Producer captchaProducer;
    private final Producer captchaProducerMath;

    /**
     * 构造验证码服务。
     *
     * @param captchaProperties  验证码配置（是否启用、类型）
     * @param captchaRepository  验证码存储端口
     * @param captchaProducer    字符验证码生成器
     * @param captchaProducerMath 算术验证码生成器
     */
    public CaptchaService(
            CaptchaProperties captchaProperties,
            CaptchaRepository captchaRepository,
            @Qualifier("captchaProducer") Producer captchaProducer,
            @Qualifier("captchaProducerMath") Producer captchaProducerMath) {
        this.captchaProperties = captchaProperties;
        this.captchaRepository = captchaRepository;
        this.captchaProducer = captchaProducer;
        this.captchaProducerMath = captchaProducerMath;
    }

    /**
     * 生成验证码。
     * <p>算术类型返回格式如 {@code "1+2=?@3"}，@ 前为展示算式，@ 后为正确答案；
     * 字符类型直接返回生成的文本。无论哪种类型，存储进 Redis 的均为用户应当输入的正确答案。</p>
     *
     * @return 验证码视图对象（uuid + base64 图片 + 是否启用）
     */
    public CaptchaVO create() {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String capStr;
        String code;
        BufferedImage image;
        // 算术类型：capStr 形如 "1+2=?"，code 为正确答案
        if ("math".equals(captchaProperties.getType())) {
            String capText = captchaProducerMath.createText();
            String[] textArr = capText.split("@");
            capStr = textArr[0];
            code = textArr[1];
            image = captchaProducerMath.createImage(capStr);
        } else {
            // 字符类型：展示文本与答案一致
            capStr = code = captchaProducer.createText();
            image = captchaProducer.createImage(capStr);
        }
        captchaRepository.save(uuid, code);
        return CaptchaVO.builder()
                .uuid(uuid)
                .img(toBase64(image))
                .enabled(captchaProperties.isEnabled())
                .build();
    }

    /**
     * 校验验证码。
     * <p>当验证码未开启时直接放行；否则校验 uuid 与 code 是否匹配且未过期，校验成功后即消费（删除）。</p>
     *
     * @param uuid 验证码唯一标识
     * @param code 用户输入的验证码
     * @return 校验通过返回 true
     */
    public boolean validate(String uuid, String code) {
        if (!captchaProperties.isEnabled()) {
            return true;
        }
        if (uuid == null || code == null) {
            return false;
        }
        return captchaRepository.validateAndConsume(uuid, code.trim());
    }

    /**
     * 将图片转为 Base64 字符串（带 data URI 前缀）。
     */
    private String toBase64(BufferedImage image) {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", os);
            byte[] bytes = os.toByteArray();
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            throw new RuntimeException("验证码图片生成失败", e);
        }
    }
}
