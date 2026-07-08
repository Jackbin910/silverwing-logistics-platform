package com.silverwing.biz.ai.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 数据库表结构定义实体
 * 用于存储可查询的数据库表信息
 */
@Data
@TableName("db_table_schema")
public class DbTableSchema {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 数据库名称
     */
    private String databaseName;

    /**
     * 表名称
     */
    private String tableName;

    /**
     * 表中文描述
     */
    private String tableComment;

    /**
     * 列名称
     */
    private String columnName;

    /**
     * 列中文描述
     */
    private String columnComment;

    /**
     * 数据类型
     */
    private String dataType;

    /**
     * 是否主键 (YES/NO)
     */
    private String isPrimaryKey;

    /**
     * 是否可为空 (YES/NO)
     */
    private String isNullable;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
