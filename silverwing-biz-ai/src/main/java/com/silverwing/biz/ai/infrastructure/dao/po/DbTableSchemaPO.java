package com.silverwing.biz.ai.infrastructure.dao.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 数据库表结构持久化对象（PO）
 * <p>
 * 与数据库表 db_table_schema 一一对应，仅承载数据，不包含领域行为。
 * 通过 DbTableSchemaInfraConvertor 与领域实体（DbTableSchemaAggregate）互转。
 * </p>
 */
@Data
@TableName(value = "db_table_schema")
public class DbTableSchemaPO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String databaseName;
    private String tableName;
    private String tableComment;
    private String columnName;
    private String columnComment;
    private String dataType;
    private String isPrimaryKey;
    private String isNullable;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
