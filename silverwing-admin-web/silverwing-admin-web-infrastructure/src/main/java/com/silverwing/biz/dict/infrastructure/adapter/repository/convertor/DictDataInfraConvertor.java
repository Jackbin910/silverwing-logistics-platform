package com.silverwing.biz.dict.infrastructure.adapter.repository.convertor;

import com.silverwing.biz.dict.domain.model.aggregate.SysDictDataAggregate;
import com.silverwing.biz.dict.infrastructure.dao.po.SysDictDataPO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 字典数据 PO 与聚合根互转。
 */
@Mapper
public interface DictDataInfraConvertor {

    DictDataInfraConvertor INSTANCE = Mappers.getMapper(DictDataInfraConvertor.class);

    SysDictDataPO toPo(SysDictDataAggregate aggregate);

    SysDictDataAggregate toDomain(SysDictDataPO po);
}
