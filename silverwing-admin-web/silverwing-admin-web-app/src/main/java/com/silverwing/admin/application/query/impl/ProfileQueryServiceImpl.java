package com.silverwing.admin.application.query.impl;

import com.silverwing.admin.application.dto.PostResponse;
import com.silverwing.admin.application.dto.ProfileResponse;
import com.silverwing.admin.application.dto.RoleResponse;
import com.silverwing.admin.application.dto.UserResponse;
import com.silverwing.admin.application.query.ProfileQueryService;
import com.silverwing.admin.client.IamPostClient;
import com.silverwing.admin.client.IamRoleClient;
import com.silverwing.admin.client.IamUserClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 个人中心查询服务实现（CQRS 读侧）
 * <p>委托防腐层端口完成查询，本类不再依赖 biz-iam 仓储与聚合根。</p>
 *
 * @author silverwing
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileQueryServiceImpl implements ProfileQueryService {

    private final IamUserClient iamUserClient;

    private final IamRoleClient iamRoleClient;

    private final IamPostClient iamPostClient;

    @Override
    public ProfileResponse getProfile(Long userId) {
        UserResponse user = iamUserClient.getById(userId);
        List<Long> roleIds = iamUserClient.getUserRoleIds(userId);
        List<RoleResponse> allRoles = iamRoleClient.listAll();
        Map<Long, String> roleNameMap = allRoles.stream()
                .collect(Collectors.toMap(RoleResponse::getId, RoleResponse::getRoleName, (a, b) -> a));
        String roleGroup = roleIds.stream()
                .map(roleNameMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.joining(","));

        List<Long> postIds = iamPostClient.getUserPostIds(userId);
        List<PostResponse> allPosts = iamPostClient.optionSelect();
        Map<Long, String> postNameMap = allPosts.stream()
                .collect(Collectors.toMap(PostResponse::getId, PostResponse::getPostName, (a, b) -> a));
        String postGroup = postIds.stream()
                .map(postNameMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.joining(","));

        return ProfileResponse.builder()
                .user(user)
                .roleGroup(roleGroup)
                .postIds(postIds)
                .postGroup(postGroup)
                .build();
    }
}
