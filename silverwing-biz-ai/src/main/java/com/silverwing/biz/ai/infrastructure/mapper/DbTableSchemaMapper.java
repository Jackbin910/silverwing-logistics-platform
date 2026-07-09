package com.silverwing.biz.ai.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.silverwing.biz.ai.infrastructure.dao.po.DbTableSchemaPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 数据库表结构Mapper
 * <p>
 * 自定义查询的 SQL 定义在对应的 DbTableSchemaMapper.xml 中（对齐 biz-iam 的 XML 模式）。
 * </p>
 */
@Mapper
public interface DbTableSchemaMapper extends BaseMapper<DbTableSchemaPO> {

    /**
     * 获取指定数据库的所有表结构信息
     *
     * @param databaseName 数据库名称
     * @return 表结构列表
     */
    List<DbTableSchemaPO> getTables(@Param("databaseName") String databaseName);

    /**
     * 获取指定表的列信息
     *
     * @param databaseName 数据库名称
     * @param tableName   表名称
     * @return 列信息列表
     */
    List<DbTableSchemaPO> getTableColumns(@Param("databaseName") String databaseName,
                                          @Param("tableName") String tableName);
}
