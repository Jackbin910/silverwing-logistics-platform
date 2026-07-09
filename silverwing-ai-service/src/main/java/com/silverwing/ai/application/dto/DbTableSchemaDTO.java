package com.silverwing.ai.application.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 数据库表结构响应DTO
 * <p>由 DbTableSchemaAggregate 经 AiConvertor 映射得到，作为对外展示的表结构视图，不暴露领域聚合根。</p>
 */
@Data
public class DbTableSchemaDTO implements Serializable {

    private Long id;
    private String databaseName;
    private String tableName;
    private String tableComment;
    private String columnName;
    private String columnComment;
    private String dataType;
    private String isPrimaryKey;
    private String isNullable;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
