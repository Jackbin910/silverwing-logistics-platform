package com.silverwing.admin.application.convertor;

import com.silverwing.admin.application.command.SavePermissionCommand;
import com.silverwing.biz.iam.domain.model.aggregate.SysPermissionAggregate;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * 权限应用层转换器
 * <p>
 * 负责保存权限命令到领域实体（SysPermissionAggregate）的映射，使用 MapStruct 生成代码。
 * 缺省值（parentId/sort 默认为 0）与状态切换（enable/disable）由 {@code @AfterMapping} 处理，
 * 以保留原转换器的语义。
 * </p>
 */
@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PermissionConvertor {

    /**
     * 将保存权限命令应用到已有实体（仅覆盖非空字段，缺省值与状态由领域行为处理）
     */
    @Mapping(target = "id", ignore = true)
    void applyCommandToEntity(@MappingTarget SysPermissionAggregate entity, SavePermissionCommand cmd);

    /**
     * 补全缺省值并据状态启用/禁用；visible、isRefresh 保持原命令值（含 null）
     */
    @AfterMapping
    default void after(SavePermissionCommand cmd, @MappingTarget SysPermissionAggregate entity) {
        if (cmd.getParentId() != null) {
            entity.setParentId(cmd.getParentId());
        } else {
            entity.setParentId(0L);
        }
        if (cmd.getSort() != null) {
            entity.setSort(cmd.getSort());
        } else {
            entity.setSort(0);
        }
        if (cmd.getStatus() != null) {
            if (cmd.getStatus() == 1) {
                entity.enable();
            } else {
                entity.disable();
            }
        }
        entity.setVisible(cmd.getVisible());
        entity.setIsRefresh(cmd.getIsRefresh());
    }
}
