package com.silverwing.admin.client.convertor;

import com.silverwing.admin.application.command.SaveDictDataCommand;
import com.silverwing.admin.application.dto.DictDataResponse;
import com.silverwing.biz.dict.domain.model.aggregate.SysDictDataAggregate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * 字典数据命令 / 聚合根 / 响应互转（防腐层）。
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DictDataConvertor {

    /** 新增场景：命令转换为聚合根（忽略主键，由数据库生成） */
    @Mapping(target = "id", ignore = true)
    SysDictDataAggregate toEntity(SaveDictDataCommand command);

    /** 更新场景：将命令字段合并到已有聚合根（忽略主键，保留原有主键） */
    @Mapping(target = "id", ignore = true)
    void applyCommandToEntity(@MappingTarget SysDictDataAggregate entity, SaveDictDataCommand command);

    /** 聚合根转换为响应体 */
    DictDataResponse toResponse(SysDictDataAggregate aggregate);
}
