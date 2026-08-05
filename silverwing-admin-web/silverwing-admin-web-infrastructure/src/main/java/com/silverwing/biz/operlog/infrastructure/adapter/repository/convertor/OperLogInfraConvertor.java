package com.silverwing.biz.operlog.infrastructure.adapter.repository.convertor;

import com.silverwing.biz.operlog.domain.model.aggregate.SysOperLogAggregate;
import com.silverwing.biz.iam.infrastructure.dao.po.SysOperLogPO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

/**
 * 操作日志 PO 与聚合根互转（基础设施层内部使用，采用 INSTANCE 静态实例）。
 * <p>
 * 数据库主键列为 {@code id}，聚合根暴露为业务语义的 {@code operId}，需显式映射。
 * </p>
 */
@Mapper
public interface OperLogInfraConvertor {

    OperLogInfraConvertor INSTANCE = Mappers.getMapper(OperLogInfraConvertor.class);

    /** PO 转换为聚合根 */
    @Mapping(source = "id", target = "operId")
    SysOperLogAggregate toDomain(SysOperLogPO po);

    /** 聚合根转换为 PO */
    @Mapping(source = "operId", target = "id")
    SysOperLogPO toPo(SysOperLogAggregate aggregate);

    /** 将聚合根字段合并到已有 PO（忽略主键） */
    @Mapping(target = "id", ignore = true)
    void merge(@MappingTarget SysOperLogPO po, SysOperLogAggregate aggregate);
}
