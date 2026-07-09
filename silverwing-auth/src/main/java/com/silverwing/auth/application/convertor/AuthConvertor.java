package com.silverwing.auth.application.convertor;

import com.silverwing.auth.application.dto.AuthUserInfo;
import com.silverwing.biz.iam.domain.model.aggregate.SysUserAggregate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 认证应用层转换器
 * <p>
 * 负责将 biz-iam 领域对象（SysUserAggregate）与角色/权限编码聚合并转换为应用层 DTO，
 * 隔离领域模型与对外传输模型，对齐 kaleido 的 application/convertor 分层。
 * </p>
 */
@Component
@RequiredArgsConstructor
public class AuthConvertor {

    /**
     * 将领域用户与角色/权限编码转换为对外用户信息 DTO
     *
     * @param user        领域用户对象（可为 null，降级为基础信息）
     * @param roleCodes   角色编码列表
     * @param permissions 权限编码列表
     * @return 用户信息 DTO
     */
    public AuthUserInfo toAuthUserInfo(SysUserAggregate user, List<String> roleCodes,
            List<String> permissions) {
        if (user == null) {
            // 降级：返回基础信息
            return AuthUserInfo.builder()
                    .roles(roleCodes)
                    .permissions(permissions)
                    .build();
        }

        return AuthUserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .avatar(user.getAvatar())
                .email(user.getEmail())
                .phone(user.getPhone())
                .roles(roleCodes)
                .permissions(permissions)
                .build();
    }
}
