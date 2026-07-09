package com.silverwing.ai.application.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

/**
 * 数据库查询执行服务
 * 负责执行SQL并返回查询结果
 */
@Slf4j
@Service
public class DatabaseQueryService {

    private JdbcTemplate jdbcTemplate;

    /**
     * 默认最大返回行数
     */
    private static final int MAX_ROWS = 100;

    /**
     * 自动装配JdbcTemplate
     *
     * @param dataSource 数据源
     */
    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /**
     * 执行SQL查询
     *
     * @param sql SQL查询语句
     * @return 查询结果列表
     */
    public List<Map<String, Object>> executeQuery(String sql) {
        try {
            log.info("执行SQL查询: {}", sql);

            // 限制返回行数
            String limitedSql = addLimit(sql, MAX_ROWS);

            List<Map<String, Object>> results = jdbcTemplate.queryForList(limitedSql);

            log.info("查询返回 {} 条记录", results.size());
            return results;

        } catch (Exception e) {
            log.error("SQL执行失败: {}", sql, e);
            throw new RuntimeException("SQL执行失败: " + e.getMessage(), e);
        }
    }

    /**
     * 执行SQL并返回结果数量
     *
     * @param sql SQL查询语句
     * @return 结果数量
     */
    public int executeCount(String sql) {
        try {
            // 包装为COUNT查询
            String countSql = "SELECT COUNT(*) as cnt FROM (" + sql + ") as t";
            Map<String, Object> result = jdbcTemplate.queryForMap(countSql);
            return ((Number) result.get("cnt")).intValue();

        } catch (Exception e) {
            log.error("COUNT查询失败: {}", sql, e);
            return 0;
        }
    }

    /**
     * 为SQL添加LIMIT限制
     *
     * @param sql    原始SQL
     * @param limit  最大行数
     * @return 添加LIMIT后的SQL
     */
    private String addLimit(String sql, int limit) {
        // 如果已经有LIMIT子句，不处理
        if (sql.toUpperCase().contains("LIMIT")) {
            return sql;
        }

        // 如果有ORDER BY，LIMIT放在ORDER BY之后
        if (sql.toUpperCase().contains("ORDER BY")) {
            return sql + " LIMIT " + limit;
        }

        // 其他情况直接添加LIMIT
        return sql + " LIMIT " + limit;
    }

    /**
     * 验证SQL是否可执行
     *
     * @param sql SQL语句
     * @return 是否可执行
     */
    public boolean canExecute(String sql) {
        if (sql == null || sql.isBlank()) {
            return false;
        }

        String upperSql = sql.toUpperCase().trim();
        return upperSql.startsWith("SELECT");
    }

    /**
     * 获取查询结果的格式化描述
     *
     * @param results 查询结果
     * @return 格式化描述
     */
    public String formatResults(List<Map<String, Object>> results) {
        if (results == null || results.isEmpty()) {
            return "查询结果为空";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("共查询到 ").append(results.size()).append(" 条记录：\n\n");

        for (int i = 0; i < results.size(); i++) {
            Map<String, Object> row = results.get(i);
            sb.append("【记录 ").append(i + 1).append("】\n");

            row.forEach((key, value) -> {
                sb.append("  - ").append(key).append(": ").append(value).append("\n");
            });

            sb.append("\n");
        }

        return sb.toString();
    }
}
