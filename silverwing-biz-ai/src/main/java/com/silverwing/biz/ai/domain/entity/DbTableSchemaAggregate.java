package com.silverwing.biz.ai.domain.entity;

import com.silverwing.common.entity.DomainEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 数据库表结构聚合根
 * <p>
 * 封装可查询的数据库表结构信息领域模型；持久化映射由基础设施层
 * DbTableSchemaPO（@TableName）承担，聚合根本身不持有表注解。
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DbTableSchemaAggregate extends DomainEntity {

    private Long id;

    /** 数据库名称 */
    private String databaseName;

    /** 表名称 */
    private String tableName;

    /** 表中文描述 */
    private String tableComment;

    /** 列名称 */
    private String columnName;

    /** 列中文描述 */
    private String columnComment;

    /** 数据类型 */
    private String dataType;

    /** 是否主键 (YES/NO) */
    private String isPrimaryKey;

    /** 是否可为空 (YES/NO) */
    private String isNullable;
}
