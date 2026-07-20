package com.silverwing.ai.domain.service.rag;

import com.silverwing.ai.application.convertor.AiConvertor;
import com.silverwing.ai.application.dto.DbTableSchemaDTO;
import com.silverwing.biz.ai.domain.repository.DbTableSchemaRepository;
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
 * 负责存储和检索数据库表结构信息；经 AiConvertor 将聚合根转换为对外 DTO。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DbSchemaService {

    private final DbTableSchemaRepository dbTableSchemaRepository;
    private final AiConvertor aiConvertor;

    /**
     * 获取所有已注册的表结构信息（对外以 DTO 呈现）
     *
     * @return 表结构 DTO 列表
     */
    public List<DbTableSchemaDTO> getAllTableSchemas() {
        try {
            return dbTableSchemaRepository.listAll().stream()
                    .map(aiConvertor::toDbTableSchemaDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取表结构信息失败", e);
            return initDefaultSchemas();
        }
    }

    /**
     * 获取指定表的结构信息（对外以 DTO 呈现）
     *
     * @param tableName 表名称
     * @return 表结构 DTO 列表
     */
    public List<DbTableSchemaDTO> getTableSchema(String tableName) {
        return dbTableSchemaRepository.getByTableName(tableName).stream()
                .map(aiConvertor::toDbTableSchemaDto)
                .collect(Collectors.toList());
    }

    /**
     * 生成表结构的文本描述（用于LLM理解）
     * 使用JetCache缓存，避免每次查询都重新构建描述
     *
     * @return 格式化的表结构描述
     */
    @Cached(name = "db:schema:desc", expire = 3600, cacheType = CacheType.BOTH, localLimit = 50)
    public String generateSchemaDescription() {
        List<DbTableSchemaDTO> schemas = getAllTableSchemas();

        if (schemas.isEmpty()) {
            return "暂无表结构信息";
        }

        // 按表分组
        return schemas.stream()
                .collect(Collectors.groupingBy(DbTableSchemaDTO::getTableName))
                .entrySet()
                .stream()
                .map(entry -> {
                    String tableName = entry.getKey();
                    List<DbTableSchemaDTO> columns = entry.getValue();
                    String tableComment = columns.get(0).getTableComment();

                    StringBuilder sb = new StringBuilder();
                    sb.append("## ").append(tableName).append(" (").append(tableComment).append(")\n");
                    sb.append("| 列名 | 类型 | 说明 |\n");
                    sb.append("|------|------|------|\n");

                    for (DbTableSchemaDTO column : columns) {
                        sb.append(" | ")
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
     *
     * @return 空 DTO 列表
     */
    private List<DbTableSchemaDTO> initDefaultSchemas() {
        log.info("数据库表结构为空，返回空列表等待数据初始化");
        return new ArrayList<>();
    }
}
