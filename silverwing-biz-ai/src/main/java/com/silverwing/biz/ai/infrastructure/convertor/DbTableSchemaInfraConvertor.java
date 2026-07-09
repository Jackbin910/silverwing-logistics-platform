package com.silverwing.biz.ai.infrastructure.convertor;

import com.silverwing.biz.ai.domain.entity.DbTableSchemaAggregate;
import com.silverwing.biz.ai.infrastructure.dao.po.DbTableSchemaPO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 数据库表结构基础设施转换器（防腐层）
 * <p>负责 PO（DbTableSchemaPO）与领域实体（DbTableSchemaAggregate）互转，使用 MapStruct 编译期生成。</p>
 */
@Mapper
public interface DbTableSchemaInfraConvertor {

    /** 静态单例，供仓储实现直接调用 */
    DbTableSchemaInfraConvertor INSTANCE = Mappers.getMapper(DbTableSchemaInfraConvertor.class);

    /** 领域实体 -> 持久化对象 */
    DbTableSchemaPO toPo(DbTableSchemaAggregate aggregate);

    /** 持久化对象 -> 领域实体 */
    DbTableSchemaAggregate toDomain(DbTableSchemaPO po);
}
