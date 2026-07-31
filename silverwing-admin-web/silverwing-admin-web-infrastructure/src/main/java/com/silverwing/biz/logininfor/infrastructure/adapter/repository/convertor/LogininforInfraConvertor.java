package com.silverwing.biz.logininfor.infrastructure.adapter.repository.convertor;

import com.silverwing.biz.logininfor.domain.model.aggregate.SysLogininforAggregate;
import com.silverwing.biz.logininfor.infrastructure.dao.po.SysLogininforPO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

/**
 * 登录日志 PO 与聚合根互转（基础设施层内部使用，采用 INSTANCE 静态实例）。
 */
@Mapper
public interface LogininforInfraConvertor {

    LogininforInfraConvertor INSTANCE = Mappers.getMapper(LogininforInfraConvertor.class);

    /** PO 转换为聚合根 */
    SysLogininforAggregate toDomain(SysLogininforPO po);

    /** 聚合根转换为 PO */
    SysLogininforPO toPo(SysLogininforAggregate aggregate);

    /** 将聚合根字段合并到已有 PO（忽略主键） */
    void merge(@MappingTarget SysLogininforPO po, SysLogininforAggregate aggregate);
}
