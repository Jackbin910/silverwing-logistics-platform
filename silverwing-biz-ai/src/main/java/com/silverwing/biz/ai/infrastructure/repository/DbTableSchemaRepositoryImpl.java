package com.silverwing.biz.ai.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.silverwing.biz.ai.domain.entity.DbTableSchemaAggregate;
import com.silverwing.biz.ai.domain.repository.DbTableSchemaRepository;
import com.silverwing.biz.ai.infrastructure.convertor.DbTableSchemaInfraConvertor;
import com.silverwing.biz.ai.infrastructure.dao.po.DbTableSchemaPO;
import com.silverwing.biz.ai.infrastructure.mapper.DbTableSchemaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据库表结构仓储实现（基于 MyBatis-Plus Mapper）
 * <p>经 DbTableSchemaInfraConvertor 在 PO 与领域聚合根之间转换，落实防腐。</p>
 */
@Repository
@RequiredArgsConstructor
public class DbTableSchemaRepositoryImpl implements DbTableSchemaRepository {

    private final DbTableSchemaMapper dbTableSchemaMapper;

    @Override
    public List<DbTableSchemaAggregate> listAll() {
        return dbTableSchemaMapper.selectList(null).stream()
                .map(DbTableSchemaInfraConvertor.INSTANCE::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<DbTableSchemaAggregate> getByTableName(String tableName) {
        LambdaQueryWrapper<DbTableSchemaPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DbTableSchemaPO::getTableName, tableName);
        return dbTableSchemaMapper.selectList(wrapper).stream()
                .map(DbTableSchemaInfraConvertor.INSTANCE::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<DbTableSchemaAggregate> getTables(String databaseName) {
        return dbTableSchemaMapper.getTables(databaseName).stream()
                .map(DbTableSchemaInfraConvertor.INSTANCE::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<DbTableSchemaAggregate> getTableColumns(String databaseName, String tableName) {
        return dbTableSchemaMapper.getTableColumns(databaseName, tableName).stream()
                .map(DbTableSchemaInfraConvertor.INSTANCE::toDomain)
                .collect(Collectors.toList());
    }
}
