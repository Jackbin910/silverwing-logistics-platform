package com.silverwing.biz.ai.domain.repository;

import com.silverwing.biz.ai.domain.entity.DbTableSchemaAggregate;

import java.util.List;

/**
 * 数据库表结构领域仓储接口
 */
public interface DbTableSchemaRepository {

    /**
     * 查询全部表结构
     *
     * @return 表结构列表
     */
    List<DbTableSchemaAggregate> listAll();

    /**
     * 按表名查询表结构
     *
     * @param tableName 表名称
     * @return 表结构列表
     */
    List<DbTableSchemaAggregate> getByTableName(String tableName);

    /**
     * 获取指定数据库的所有表信息
     *
     * @param databaseName 数据库名称
     * @return 表信息列表
     */
    List<DbTableSchemaAggregate> getTables(String databaseName);

    /**
     * 获取指定表的列信息
     *
     * @param databaseName 数据库名称
     * @param tableName   表名称
     * @return 列信息列表
     */
    List<DbTableSchemaAggregate> getTableColumns(String databaseName, String tableName);
}
