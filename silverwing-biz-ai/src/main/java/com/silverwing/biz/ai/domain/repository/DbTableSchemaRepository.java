package com.silverwing.biz.ai.domain.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.silverwing.biz.ai.domain.entity.DbTableSchema;

import java.util.List;

/**
 * 数据库表结构领域仓储接口
 */
public interface DbTableSchemaRepository {

    /**
     * 根据条件查询表结构（wrapper 为 null 时返回全量）
     *
     * @param wrapper 查询条件
     * @return 表结构列表
     */
    List<DbTableSchema> list(LambdaQueryWrapper<DbTableSchema> wrapper);

    /**
     * 获取指定数据库的所有表信息
     *
     * @param databaseName 数据库名称
     * @return 表信息列表
     */
    List<DbTableSchema> getTables(String databaseName);

    /**
     * 获取指定表的列信息
     *
     * @param databaseName 数据库名称
     * @param tableName   表名称
     * @return 列信息列表
     */
    List<DbTableSchema> getTableColumns(String databaseName, String tableName);
}
