package com.silverwing.biz.ai.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.silverwing.biz.ai.domain.entity.DbTableSchema;
import com.silverwing.biz.ai.domain.repository.DbTableSchemaRepository;
import com.silverwing.biz.ai.infrastructure.mapper.DbTableSchemaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 数据库表结构仓储实现（基于 MyBatis-Plus Mapper）
 */
@Repository
@RequiredArgsConstructor
public class DbTableSchemaRepositoryImpl implements DbTableSchemaRepository {

    private final DbTableSchemaMapper dbTableSchemaMapper;

    @Override
    public List<DbTableSchema> list(LambdaQueryWrapper<DbTableSchema> wrapper) {
        return dbTableSchemaMapper.selectList(wrapper);
    }

    @Override
    public List<DbTableSchema> getTables(String databaseName) {
        return dbTableSchemaMapper.getTables(databaseName);
    }

    @Override
    public List<DbTableSchema> getTableColumns(String databaseName, String tableName) {
        return dbTableSchemaMapper.getTableColumns(databaseName, tableName);
    }
}
