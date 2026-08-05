package com.silverwing.auth.iam.infrastructure.adapter.repository;

import com.silverwing.auth.iam.domain.adapter.repository.PasswordRetryRepository;
import com.silverwing.common.constant.RedisConstants;
import com.silverwing.common.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * 密码错误重试计数仓储实现（Redis）。
 * <p>错误次数存于 {@code silverwing:pwd:err:{username}}，并设置锁定窗口过期时间，
 * 到期自动清除，等价于账号解锁。</p>
 *
 * @author silverwing
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class RedisPasswordRetryRepository implements PasswordRetryRepository {

    private final RedisUtil redisUtil;

    private String keyOf(String username) {
        return RedisConstants.PWD_ERR_CNT_PREFIX + username;
    }

    @Override
    public int getRetryCount(String username) {
        Object value = redisUtil.get(keyOf(username));
        if (value == null) {
            return 0;
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Override
    public int incrementAndGet(String username) {
        String key = keyOf(username);
        // 在已有计数上 +1，并以写操作重置锁定窗口的过期时间，保证 TTL 始终有效
        int count = getRetryCount(username) + 1;
        redisUtil.set(key, count, RedisConstants.PASSWORD_LOCK_SECONDS);
        return count;
    }

    @Override
    public void clear(String username) {
        redisUtil.delete(keyOf(username));
    }

    @Override
    public boolean hasKey(String username) {
        Boolean exist = redisUtil.hasKey(keyOf(username));
        return Boolean.TRUE.equals(exist);
    }
}
