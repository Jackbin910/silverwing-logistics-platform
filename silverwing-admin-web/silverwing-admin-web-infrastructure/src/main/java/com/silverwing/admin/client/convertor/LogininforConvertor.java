package com.silverwing.admin.client.convertor;

import com.silverwing.admin.application.dto.LogininforResponse;
import com.silverwing.biz.logininfor.domain.model.aggregate.SysLogininforAggregate;
import org.mapstruct.Mapper;

/**
 * 系统访问记录聚合根 / 响应互转（防腐层）。
 *
 * @author silverwing
 */
@Mapper(componentModel = "spring")
public interface LogininforConvertor {

    /**
     * 聚合根转换为响应 DTO。
     *
     * @param aggregate 聚合根
     * @return 响应 DTO
     */
    LogininforResponse toResponse(SysLogininforAggregate aggregate);
}
