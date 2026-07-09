package com.silverwing.admin.application.convertor;

import com.silverwing.admin.application.command.CreateUserCommand;
import com.silverwing.admin.application.command.UpdateUserCommand;
import com.silverwing.admin.application.dto.UserResponse;
import com.silverwing.biz.iam.domain.model.aggregate.SysUserAggregate;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * 用户应用层转换器
 * <p>
 * application/convertor 分层（MapStruct + Spring 注入）。
 * 密码加密（changePassword）与默认启用（enable）属于领域行为，
 * 交由 {@code @AfterMapping} 在映射完成后调用，避免泄露到转换层。
 * </p>
 */
@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserConvertor {

    /**
     * 将创建用户命令转换为领域实体（密码与状态由领域行为补充）
     */
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "salt", ignore = true)
    @Mapping(target = "status", ignore = true)
    SysUserAggregate toEntity(CreateUserCommand command);

    /**
     * 将更新用户命令应用到已有实体（仅覆盖非空字段，状态由领域行为处理）
     */
    @Mapping(target = "status", ignore = true)
    void applyUpdate(@MappingTarget SysUserAggregate user, UpdateUserCommand command);

    /**
     * 创建场景：密码加密并默认启用
     */
    @AfterMapping
    default void afterCreate(CreateUserCommand command, @MappingTarget SysUserAggregate user) {
        user.changePassword(command.getPassword());
        user.enable();
    }

    /**
     * 更新场景：根据状态字段切换启用/禁用
     */
    @AfterMapping
    default void afterUpdate(UpdateUserCommand command, @MappingTarget SysUserAggregate user) {
        if (command.getStatus() != null) {
            if (command.getStatus() == 1) {
                user.enable();
            } else {
                user.disable();
            }
        }
    }

    /**
     * 将用户聚合根转换为对外响应DTO（不含密码与盐值）
     */
    UserResponse toResponse(SysUserAggregate user);
}
