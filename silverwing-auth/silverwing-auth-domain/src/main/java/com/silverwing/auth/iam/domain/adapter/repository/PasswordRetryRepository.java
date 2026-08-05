package com.silverwing.auth.iam.domain.adapter.repository;

/**
 * 密码错误重试计数仓储端口（领域契约）。
 * <p>基于 Redis 记录账号的连续密码错误次数，达到阈值即视为锁定。</p>
 *
 * @author silverwing
 */
public interface PasswordRetryRepository {

    /**
     * 获取当前密码错误次数（不存在返回 0）。
     *
     * @param username 用户账号
     * @return 错误次数
     */
    int getRetryCount(String username);

    /**
     * 错误次数自增一并返回最新值，并重置锁定过期时间。
     *
     * @param username 用户账号
     * @return 自增后的错误次数
     */
    int incrementAndGet(String username);

    /**
     * 清除指定账号的密码错误计数（解锁/登录成功时调用）。
     *
     * @param username 用户账号
     */
    void clear(String username);

    /**
     * 是否存在密码错误计数缓存（用于判断是否处于锁定观察窗口）。
     *
     * @param username 用户账号
     * @return 存在返回 true
     */
    boolean hasKey(String username);
}
