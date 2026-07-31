package com.silverwing.biz.dict.infrastructure.adapter.repository.convertor;

import com.silverwing.biz.dict.domain.model.aggregate.SysDictTypeAggregate;
import com.silverwing.biz.dict.infrastructure.dao.po.SysDictTypePO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 字典类型 PO 与聚合根互转。
 */
@Mapper
public interface DictTypeInfraConvertor {

    DictTypeInfraConvertor INSTANCE = Mappers.getMapper(DictTypeInfraConvertor.class);

    SysDictTypePO toPo(SysDictTypeAggregate aggregate);

    SysDictTypeAggregate toDomain(SysDictTypePO po);
}
