package com.silverwing.admin.client.convertor;

import com.silverwing.admin.application.dto.OperLogResponse;
import com.silverwing.biz.operlog.domain.model.aggregate.SysOperLogAggregate;
import org.mapstruct.Mapper;

/**
 * 操作日志聚合根 / 响应互转（防腐层）。
 */
@Mapper(componentModel = "spring")
public interface OperLogConvertor {

    /** 聚合根转换为响应 */
    OperLogResponse toResponse(SysOperLogAggregate aggregate);
}
