package com.silverwing.admin.client.convertor;

import com.silverwing.admin.application.command.SaveDeptCommand;
import com.silverwing.admin.application.dto.DeptResponse;
import com.silverwing.biz.dept.domain.model.aggregate.SysDeptAggregate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * 部门命令 / 聚合根 / 响应互转（防腐层）。
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DeptConvertor {

    /** 新增场景：命令转换为聚合根（忽略主键） */
    @Mapping(target = "deptId", ignore = true)
    SysDeptAggregate toEntity(SaveDeptCommand command);

    /** 更新场景：将命令字段合并到已有聚合根（保留主键） */
    void applyCommandToEntity(@MappingTarget SysDeptAggregate entity, SaveDeptCommand command);

    /** 聚合根转换为响应 */
    DeptResponse toResponse(SysDeptAggregate aggregate);
}
