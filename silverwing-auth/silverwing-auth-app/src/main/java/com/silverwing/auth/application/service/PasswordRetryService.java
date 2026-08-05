package com.silverwing.auth.application.service;

import com.silverwing.auth.iam.domain.adapter.repository.LogininforRepository;
import com.silverwing.auth.iam.domain.adapter.repository.PasswordRetryRepository;
import com.silverwing.auth.iam.domain.model.aggregate.LogininforAggregate;
import com.silverwing.common.constant.RedisConstants;
import com.silverwing.common.exception.BusinessException;
import com.silverwing.common.domain.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 密码错误重试与账号锁定服务。
 * <p>对应 RuoYi 的 {@code SysPasswordService}：连续密码错误达到阈值后锁定账号一段时间，
 * 登录成功或管理员解锁后清除计数。每次校验都会写入登录日志。</p>
 *
 * @author silverwing
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordRetryService {

    private final PasswordRetryRepository passwordRetryRepository;

    private final LogininforRepository logininforRepository;

    /**
     * 校验密码是否需要重试控制。
     * <p>调用方已确认用户存在且未禁用，传入密码是否匹配的结果。本方法负责：
     * 已锁定时直接拒绝；密码错误时累加计数并判断是否达到锁定阈值；密码正确时清除计数。</p>
     *
     * @param username        用户账号
     * @param ipaddr          登录IP
     * @param passwordMatches 密码是否匹配
     */
    public void validate(String username, String ipaddr, boolean passwordMatches) {
        // 已处于锁定窗口且计数达到阈值，直接拒绝
        if (passwordRetryRepository.getRetryCount(username) >= RedisConstants.PASSWORD_MAX_RETRY_COUNT) {
            long lockMinutes = RedisConstants.PASSWORD_LOCK_SECONDS / 60L;
            recordLogininfor(username, ipaddr, "密码输入错误"
                    + RedisConstants.PASSWORD_MAX_RETRY_COUNT + "次，帐户锁定" + lockMinutes + "分钟");
            throw BusinessException.i18n(ResultCode.UNAUTHORIZED, "auth.login.account.locked",
                    RedisConstants.PASSWORD_MAX_RETRY_COUNT, lockMinutes);
        }

        if (!passwordMatches) {
            int retryCount = passwordRetryRepository.incrementAndGet(username);
            recordLogininfor(username, ipaddr, "密码输入错误" + retryCount + "次");
            if (retryCount >= RedisConstants.PASSWORD_MAX_RETRY_COUNT) {
                long lockMinutes = RedisConstants.PASSWORD_LOCK_SECONDS / 60L;
                throw BusinessException.i18n(ResultCode.UNAUTHORIZED, "auth.login.account.locked",
                        RedisConstants.PASSWORD_MAX_RETRY_COUNT, lockMinutes);
            }
            throw BusinessException.i18n(ResultCode.UNAUTHORIZED, "auth.login.username.or.password.error");
        }

        // 密码正确，清除错误计数
        passwordRetryRepository.clear(username);
    }

    /**
     * 管理员解锁账号：清除密码错误计数缓存。
     *
     * @param username 用户账号
     */
    public void unlock(String username) {
        passwordRetryRepository.clear(username);
    }

    /**
     * 获取当前密码错误次数。
     *
     * @param username 用户账号
     * @return 错误次数
     */
    public int getRetryCount(String username) {
        return passwordRetryRepository.getRetryCount(username);
    }

    /**
     * 账号是否处于锁定状态。
     *
     * @param username 用户账号
     * @return 锁定返回 true
     */
    public boolean isLocked(String username) {
        return passwordRetryRepository.getRetryCount(username) >= RedisConstants.PASSWORD_MAX_RETRY_COUNT;
    }

    /**
     * 写入一条密码校验相关的登录失败日志。
     *
     * @param username 用户账号
     * @param ipaddr   登录IP
     * @param msg      提示信息
     */
    private void recordLogininfor(String username, String ipaddr, String msg) {
        try {
            LogininforAggregate aggregate = new LogininforAggregate();
            aggregate.setUserName(username);
            aggregate.setIpaddr(ipaddr);
            aggregate.setStatus(1);
            aggregate.setMsg(msg);
            aggregate.setAccessTime(LocalDateTime.now());
            logininforRepository.insert(aggregate);
        } catch (Exception e) {
            log.warn("写入登录日志失败 username={}, 原因={}", username, e.getMessage());
        }
    }
}
