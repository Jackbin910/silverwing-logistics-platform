package com.silverwing.admin.client.convertor;

import com.silverwing.admin.application.command.SaveLogininforCommand;
import com.silverwing.admin.application.dto.LogininforResponse;
import com.silverwing.biz.logininfor.domain.model.aggregate.SysLogininforAggregate;
import org.mapstruct.Mapper;

/**
 * 登录日志命令 / 聚合根 / 响应互转（防腐层）。
 */
@Mapper(componentModel = "spring")
public interface LogininforConvertor {

    /** 新增场景：命令转换为聚合根 */
    SysLogininforAggregate toEntity(SaveLogininforCommand command);

    /** 聚合根转换为响应 */
    LogininforResponse toResponse(SysLogininforAggregate aggregate);
}
