package com.silverwing.auth.iam.domain.adapter.repository;

/**
 * 验证码存储端口。
 * <p>用于向底层存储（Redis）写入与校验验证码答案，与具体存储实现解耦。</p>
 */
public interface CaptchaRepository {

    /**
     * 保存验证码答案。
     *
     * @param uuid 本次验证码唯一标识
     * @param code 验证码答案（用户需要输入的文本）
     */
    void save(String uuid, String code);

    /**
     * 校验并消费验证码（校验成功后即删除，防止重放）。
     *
     * @param uuid 本次验证码唯一标识
     * @param code 用户输入的验证码答案
     * @return 校验通过返回 true，否则返回 false
     */
    boolean validateAndConsume(String uuid, String code);
}
