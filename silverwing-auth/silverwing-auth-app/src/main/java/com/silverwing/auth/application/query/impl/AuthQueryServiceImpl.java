package com.silverwing.auth.application.query.impl;

import com.silverwing.auth.application.convertor.AuthConvertor;
import com.silverwing.auth.application.dto.AuthUserInfo;
import com.silverwing.auth.application.query.AuthQueryService;
import com.silverwing.auth.iam.domain.adapter.repository.PermissionRepository;
import com.silverwing.auth.iam.domain.adapter.repository.RoleRepository;
import com.silverwing.auth.iam.domain.adapter.repository.UserRepository;
import com.silverwing.auth.iam.domain.model.aggregate.AuthRoleAggregate;
import com.silverwing.auth.iam.domain.model.aggregate.AuthUserAggregate;
import com.silverwing.common.domain.ResultCode;
import com.silverwing.common.exception.BusinessException;
import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 认证查询服务实现（CQRS 读侧）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthQueryServiceImpl implements AuthQueryService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final AuthConvertor authConvertor;

    @Override
    public AuthUserInfo getCurrentUserInfo() {
        Object loginId = StpUtil.getLoginIdDefaultNull();
        if (loginId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "auth.not.login");
        }

        Long userId = Long.parseLong(loginId.toString());
        AuthUserAggregate user = userRepository.findById(userId);

        List<String> roleCodes = roleRepository.findRolesByUserId(userId).stream()
                .map(AuthRoleAggregate::getRoleCode)
                .collect(Collectors.toList());
        List<String> permissions = permissionRepository.findPermissionCodesByUserId(userId);

        return authConvertor.toAuthUserInfo(user, roleCodes, permissions);
    }
}
