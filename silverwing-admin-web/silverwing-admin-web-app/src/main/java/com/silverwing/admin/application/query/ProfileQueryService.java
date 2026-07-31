package com.silverwing.admin.application.query;

import com.silverwing.admin.application.dto.ProfileResponse;

/**
 * 个人中心查询服务（CQRS 读侧）
 * <p>通过 {@link IamUserClient} 与 {@link com.silverwing.admin.client.IamRoleClient} 防腐层端口
 * 拼装当前登录用户的资料与角色组，不直接依赖 biz-iam 领域聚合根。</p>
 *
 * @author silverwing
 */
public interface ProfileQueryService {

    /**
     * 获取当前登录用户个人中心信息
     *
     * @param userId 当前登录用户 ID
     * @return 个人中心信息（用户资料 + 角色组）
     */
    ProfileResponse getProfile(Long userId);
}
