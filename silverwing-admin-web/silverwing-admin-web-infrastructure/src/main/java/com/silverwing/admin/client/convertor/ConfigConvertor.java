package com.silverwing.admin.client.convertor;

import com.silverwing.admin.application.command.SaveConfigCommand;
import com.silverwing.admin.application.dto.ConfigResponse;
import com.silverwing.biz.config.domain.model.aggregate.SysConfigAggregate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * 参数配置命令 / 聚合根 / 响应互转（防腐层）。
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ConfigConvertor {

    /** 新增场景：命令转换为聚合根（忽略主键，由数据库生成） */
    @Mapping(target = "id", ignore = true)
    SysConfigAggregate toEntity(SaveConfigCommand command);

    /** 更新场景：将命令字段合并到已有聚合根（忽略主键，保留原有主键） */
    @Mapping(target = "id", ignore = true)
    void applyCommandToEntity(@MappingTarget SysConfigAggregate entity, SaveConfigCommand command);

    /** 聚合根转换为响应体 */
    ConfigResponse toResponse(SysConfigAggregate aggregate);
}
