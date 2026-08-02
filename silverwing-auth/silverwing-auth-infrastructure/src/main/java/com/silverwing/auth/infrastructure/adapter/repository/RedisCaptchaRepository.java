package com.silverwing.auth.infrastructure.adapter.repository;

import com.silverwing.auth.iam.domain.adapter.repository.CaptchaRepository;
import com.silverwing.common.constant.SaSessionConstants;
import com.silverwing.common.util.RedisUtil;
import org.springframework.stereotype.Repository;

import java.util.Objects;

/**
 * 基于 Redis 的验证码存储实现。
 * <p>将验证码答案写入 {@code silverwing:captcha:{uuid}}，并设置默认 2 分钟过期；校验成功后删除该键防止重放。</p>
 */
@Repository
public class RedisCaptchaRepository implements CaptchaRepository {

    private final RedisUtil redisUtil;

    public RedisCaptchaRepository(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }

    @Override
    public void save(String uuid, String code) {
        String key = SaSessionConstants.CAPTCHA_PREFIX + uuid;
        redisUtil.set(key, code, SaSessionConstants.CAPTCHA_EXPIRE_SECONDS);
    }

    @Override
    public boolean validateAndConsume(String uuid, String code) {
        String key = SaSessionConstants.CAPTCHA_PREFIX + uuid;
        Object cached = redisUtil.get(key);
        if (Objects.isNull(cached)) {
            return false;
        }
        // 无论成功失败都删除，避免验证码被重复使用（重放）
        redisUtil.delete(key);
        return Objects.equals(cached.toString(), code);
    }
}
