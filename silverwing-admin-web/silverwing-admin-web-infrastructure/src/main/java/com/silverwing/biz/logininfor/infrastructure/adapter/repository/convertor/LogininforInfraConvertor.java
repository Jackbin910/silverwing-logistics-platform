package com.silverwing.biz.logininfor.infrastructure.adapter.repository.convertor;

import com.silverwing.biz.logininfor.domain.model.aggregate.SysLogininforAggregate;
import com.silverwing.biz.logininfor.infrastructure.dao.po.SysLogininforPO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 系统访问记录 PO 与聚合根互转（基础设施层内部使用，采用 INSTANCE 静态实例）。
 *
 * @author silverwing
 */
@Mapper
public interface LogininforInfraConvertor {

    LogininforInfraConvertor INSTANCE = Mappers.getMapper(LogininforInfraConvertor.class);

    /**
     * PO 转换为聚合根。
     *
     * @param po 持久化对象
     * @return 聚合根
     */
    SysLogininforAggregate toDomain(SysLogininforPO po);

    /**
     * 聚合根转换为 PO。
     *
     * @param aggregate 聚合根
     * @return 持久化对象
     */
    SysLogininforPO toPo(SysLogininforAggregate aggregate);
}
