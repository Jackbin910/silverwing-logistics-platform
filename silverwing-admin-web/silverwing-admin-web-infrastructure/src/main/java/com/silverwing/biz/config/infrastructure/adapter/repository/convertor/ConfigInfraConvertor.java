package com.silverwing.biz.config.infrastructure.adapter.repository.convertor;

import com.silverwing.biz.config.domain.model.aggregate.SysConfigAggregate;
import com.silverwing.biz.config.infrastructure.dao.po.SysConfigPO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

/**
 * 参数配置 PO 与聚合根互转（基础设施层内部使用，非 Spring Bean）。
 */
@Mapper
public interface ConfigInfraConvertor {

    ConfigInfraConvertor INSTANCE = Mappers.getMapper(ConfigInfraConvertor.class);

    /** PO 转换为聚合根 */
    SysConfigAggregate toDomain(SysConfigPO po);

    /** 聚合根转换为 PO */
    SysConfigPO toPo(SysConfigAggregate aggregate);

    /** 将聚合根字段合并到已有 PO（忽略主键） */
    void merge(@MappingTarget SysConfigPO po, SysConfigAggregate aggregate);
}
