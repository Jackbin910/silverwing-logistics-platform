package com.silverwing.auth.application.query;

import com.silverwing.auth.application.dto.AuthUserInfo;

/**
 * 认证查询服务（CQRS 读侧）
 * <p>
 * 定义获取当前登录用户信息等只读用例的端口，实现见 {@code impl} 包。
 * </p>
 */
public interface AuthQueryService {

    /**
     * 获取当前登录用户信息
     */
    AuthUserInfo getCurrentUserInfo();
}
