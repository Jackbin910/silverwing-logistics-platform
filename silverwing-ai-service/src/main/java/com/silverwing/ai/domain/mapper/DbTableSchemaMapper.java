package com.silverwing.ai.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.silverwing.ai.domain.entity.DbTableSchema;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 数据库表结构Mapper
 */
@Mapper
public interface DbTableSchemaMapper extends BaseMapper<DbTableSchema> {

    /**
     * 获取指定数据库的所有表结构信息
     *
     * @param databaseName 数据库名称
     * @return 表结构列表
     */
    @Select("SELECT table_name, table_comment FROM information_schema.tables " +
            "WHERE table_schema = #{databaseName}")
    List<DbTableSchema> getTables(@Param("databaseName") String databaseName);

    /**
     * 获取指定表的列信息
     *
     * @param databaseName 数据库名称
     * @param tableName   表名称
     * @return 列信息列表
     */
    @Select("SELECT " +
            "    c.column_name, " +
            "    c.data_type, " +
            "    c.column_type, " +
            "    c.column_key, " +
            "    c.is_nullable, " +
            "    c.column_comment " +
            "FROM information_schema.columns c " +
            "WHERE c.table_schema = #{databaseName} " +
            "  AND c.table_name = #{tableName} " +
            "ORDER BY c.ordinal_position")
    List<DbTableSchema> getTableColumns(@Param("databaseName") String databaseName,
                                          @Param("tableName") String tableName);
}
