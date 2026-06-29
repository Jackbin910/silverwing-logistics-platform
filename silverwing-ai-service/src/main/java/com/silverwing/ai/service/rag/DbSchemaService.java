package com.silverwing.ai.service.rag;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.silverwing.ai.domain.entity.DbTableSchema;
import com.silverwing.ai.domain.mapper.DbTableSchemaMapper;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据库Schema管理服务
 * 负责存储和检索数据库表结构信息
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DbSchemaService {

    private final DbTableSchemaMapper dbTableSchemaMapper;

    /**
     * 默认数据库名称
     */
    private static final String DEFAULT_DATABASE = "silverwing_logistics";

    /**
     * 获取所有已注册的表结构信息
     *
     * @return 表结构列表
     */
    public List<DbTableSchema> getAllTableSchemas() {
        try {
            List<DbTableSchema> schemas = dbTableSchemaMapper.selectList(null);
            return schemas;
        } catch (Exception e) {
            log.error("获取表结构信息失败", e);
            return initDefaultSchemas();
        }
    }

    /**
     * 获取指定表的结构信息
     *
     * @param tableName 表名称
     * @return 表结构列表
     */
    public List<DbTableSchema> getTableSchema(String tableName) {
        LambdaQueryWrapper<DbTableSchema> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DbTableSchema::getTableName, tableName);
        return dbTableSchemaMapper.selectList(wrapper);
    }

    /**
     * 生成表结构的文本描述（用于LLM理解）
     * 使用JetCache缓存，避免每次查询都重新构建描述
     *
     * @return 格式化的表结构描述
     */
    @Cached(name = "db:schema:desc", expire = 3600, cacheType = CacheType.BOTH, localLimit = 50)
    public String generateSchemaDescription() {
        List<DbTableSchema> schemas = getAllTableSchemas();

        if (schemas.isEmpty()) {
            return "暂无表结构信息";
        }

        // 按表分组
        return schemas.stream()
                .collect(Collectors.groupingBy(DbTableSchema::getTableName))
                .entrySet()
                .stream()
                .map(entry -> {
                    String tableName = entry.getKey();
                    List<DbTableSchema> columns = entry.getValue();
                    String tableComment = columns.get(0).getTableComment();

                    StringBuilder sb = new StringBuilder();
                    sb.append("## ").append(tableName).append(" (").append(tableComment).append(")\n");
                    sb.append("| 列名 | 类型 | 说明 |\n");
                    sb.append("|------|------|------|\n");

                    for (DbTableSchema column : columns) {
                        sb.append("| ")
                          .append(column.getColumnName())
                          .append(" | ")
                          .append(column.getDataType())
                          .append(" | ")
                          .append(column.getColumnComment())
                          .append(" |\n");
                    }

                    return sb.toString();
                })
                .collect(Collectors.joining("\n"));
    }

    /**
     * 初始化默认的表结构信息
     * 从数据库读取，如果数据库为空则返回空列表
     *
     * @return 表结构列表
     */
    private List<DbTableSchema> initDefaultSchemas() {
        log.info("数据库表结构为空，返回空列表等待数据初始化");
        return new ArrayList<>();
    }
}
