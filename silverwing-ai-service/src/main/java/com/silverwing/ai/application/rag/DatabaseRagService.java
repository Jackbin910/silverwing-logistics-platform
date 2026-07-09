package com.silverwing.ai.application.rag;

import dev.langchain4j.model.chat.ChatModel;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Database RAG服务
 * 整合Schema管理、Text-to-SQL、查询执行和结果解释
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DatabaseRagService {

    private final DbSchemaService dbSchemaService;
    private final TextToSqlService textToSqlService;
    private final DatabaseQueryService databaseQueryService;
    private final ChatModel chatModel;

    /**
     * 处理用户的自然语言查询
     * 使用JetCache缓存相同问题的查询结果，减少LLM调用
     *
     * @param question 用户问题
     * @return 回答结果
     */
    @Cached(name = "db:query:answer", key = "#question.hashCode()", expire = 300, cacheType = CacheType.BOTH, localLimit = 200)
    public String query(String question) {
        try {
            log.info("开始处理Database RAG查询: {}", question);

            // 1. 生成SQL
            String sql = textToSqlService.generateSql(question);
            log.info("生成的SQL: {}", sql);

            // 2. 验证SQL安全性
            if (!textToSqlService.isSqlSafe(sql)) {
                log.warn("SQL不安全，已拦截: {}", sql);
                return "抱歉，无法执行该查询，可能是安全限制。";
            }

            // 3. 验证SQL可执行性
            if (!databaseQueryService.canExecute(sql)) {
                return "抱歉，无法生成有效的SQL查询语句。";
            }

            // 4. 执行SQL
            List<Map<String, Object>> results = databaseQueryService.executeQuery(sql);

            // 5. 如果没有结果，直接返回提示
            if (results == null || results.isEmpty()) {
                return "查询完成，未找到匹配的订单数据。\n\n" +
                       "可能的原因：\n" +
                       "- 数据库中没有符合条件的数据\n" +
                       "- 请尝试调整查询条件\n\n" +
                       "您可以尝试：\n" +
                       "- 查询所有订单：查询所有订单\n" +
                       "- 按状态查询：查询待接单的订单";
            }

            // 6. 生成自然语言回答
            String answer = generateAnswer(question, sql, results);
            return answer;

        } catch (Exception e) {
            log.error("Database RAG查询失败: {}", question, e);
            return "抱歉，处理您的查询时遇到了技术问题：\n" + e.getMessage() + "\n\n请稍后重试或调整查询条件。";
        }
    }

    /**
     * 生成自然语言回答
     *
     * @param question 用户问题
     * @param sql      执行的SQL
     * @param results  查询结果
     * @return 自然语言回答
     */
    private String generateAnswer(String question, String sql, List<Map<String, Object>> results) {
        // 格式化结果
        String formattedResults = databaseQueryService.formatResults(results);

        // 构建Prompt
        String prompt = buildAnswerPrompt(question, formattedResults);

        // 调用LLM生成回答
        String answer = chatModel.chat(prompt);

        return answer;
    }

    /**
     * 构建答案生成的Prompt
     *
     * @param question   用户问题
     * @param resultsStr 格式化后的查询结果
     * @return Prompt
     */
    private String buildAnswerPrompt(String question, String resultsStr) {
        return """
                你是物流订单管理系统的智能助手。请根据查询结果回答用户的问题。

                【用户问题】
                %s

                【查询结果】
                %s

                【回答要求】
                1. 直接基于查询结果回答，不要编造信息
                2. 如果查询结果为空，如实告知用户
                3. 回答要简洁明了，使用中文
                4. 如果涉及订单状态，用中文描述：
                   - pending = 待接单
                   - in_progress = 进行中
                   - completed = 已完成
                   - cancelled = 已取消
                5. 如果涉及订单类型，用中文描述：
                   - surgery_material = 手术物资
                   - bulk_material = 批量物资
                   - medicine = 药品
                   - sample = 样本
                6. 如果涉及配送方式，用中文描述：
                   - robot_dog = 机器狗
                   - robot = 机器人
                   - agv = AGV
                   - manual = 人工

                【回答】
                """.formatted(question, resultsStr);
    }

    /**
     * 获取表结构信息（用于调试或展示）
     *
     * @return 表结构描述
     */
    public String getSchemaInfo() {
        return dbSchemaService.generateSchemaDescription();
    }
}
