package com.silverwing.biz.dept.infrastructure.adapter.repository.convertor;

import com.silverwing.biz.dept.domain.model.aggregate.SysDeptAggregate;
import com.silverwing.biz.dept.infrastructure.dao.po.SysDeptPO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

/**
 * 部门 PO 与聚合根互转（基础设施层内部使用，采用 INSTANCE 静态实例，非 Spring Bean）。
 */
@Mapper
public interface DeptInfraConvertor {

    DeptInfraConvertor INSTANCE = Mappers.getMapper(DeptInfraConvertor.class);

    /** PO 转换为聚合根 */
    SysDeptAggregate toDomain(SysDeptPO po);

    /** 聚合根转换为 PO */
    SysDeptPO toPo(SysDeptAggregate aggregate);

    /** 将聚合根字段合并到已有 PO（忽略主键） */
    void merge(@MappingTarget SysDeptPO po, SysDeptAggregate aggregate);
}
