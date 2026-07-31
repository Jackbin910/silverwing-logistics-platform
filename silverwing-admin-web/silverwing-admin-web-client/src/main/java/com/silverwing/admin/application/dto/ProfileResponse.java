package com.silverwing.admin.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 个人中心信息响应。
 * <p>聚合当前登录用户资料、角色组名称与岗位组名称。</p>
 *
 * @author silverwing
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse {

    /**
     * 当前登录用户信息
     */
    private UserResponse user;

    /**
     * 角色组（逗号拼接的角色名称）
     */
    private String roleGroup;

    /**
     * 已分配岗位ID集合
     */
    private List<Long> postIds;

    /**
     * 岗位组（逗号拼接的岗位名称）
     */
    private String postGroup;
}
