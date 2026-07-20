package com.silverwing.auth.application.convertor;

import com.silverwing.auth.application.dto.AuthUserInfo;
import com.silverwing.auth.iam.domain.model.aggregate.AuthUserAggregate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * 认证应用层转换器
 * <p>
 * 负责将 auth 自有 IAM 领域对象（AuthUserAggregate）与角色/权限编码聚合转换为应用层 DTO，
 * 用户为 null 时（降级场景）各用户字段自然为 null，仅填充角色与权限编码，与原有降级行为一致。
 * </p>
 */
@Mapper(componentModel = "spring")
public interface AuthConvertor {

    /**
     * 将领域用户与角色/权限编码转换为对外用户信息 DTO
     *
     * @param user        领域用户对象（可为 null，降级为基础信息）
     * @param roleCodes   角色编码列表
     * @param permissions 权限编码列表
     * @return 用户信息 DTO
     */
    @Mapping(target = "id", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "avatar", source = "user.avatar")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "phone", source = "user.phone")
    @Mapping(target = "roles", source = "roleCodes")
    AuthUserInfo toAuthUserInfo(AuthUserAggregate user, List<String> roleCodes,
            List<String> permissions);
}
