package com.silverwing.admin.application.convertor;

import com.silverwing.admin.application.command.SaveRoleCommand;
import com.silverwing.admin.application.dto.RoleResponse;
import com.silverwing.biz.iam.domain.model.aggregate.SysRoleAggregate;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * 角色应用层转换器
 * <p>负责保存角色命令到领域实体（SysRoleAggregate）的映射，使用 MapStruct 生成代码。</p>
 */
@Mapper(componentModel = "spring")
public interface RoleConvertor {

    /**
     * 将保存角色命令转换为领域实体（状态由领域行为补充）
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    SysRoleAggregate toEntity(SaveRoleCommand command);

    /**
     * 根据状态字段启用/禁用角色（缺省视为启用）
     */
    @AfterMapping
    default void after(SaveRoleCommand command, @MappingTarget SysRoleAggregate role) {
        if (command.getStatus() != null) {
            if (command.getStatus() == 1) {
                role.enable();
            } else {
                role.disable();
            }
        } else {
            role.enable();
        }
    }

    /**
     * 将角色聚合根转换为对外响应DTO（屏蔽领域聚合根，仅暴露展示字段）
     */
    RoleResponse toResponse(SysRoleAggregate role);
}
