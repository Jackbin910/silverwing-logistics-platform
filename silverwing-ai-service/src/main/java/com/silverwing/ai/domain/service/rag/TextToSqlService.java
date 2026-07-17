package com.silverwing.ai.domain.service.rag;

import dev.langchain4j.model.chat.ChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Text-to-SQL转换服务
 * 使用LLM将自然语言问题转换为SQL查询语句
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TextToSqlService {

    private final ChatModel chatModel;

    private final DbSchemaService dbSchemaService;

    /**
     * 将自然语言问题转换为SQL查询
     *
     * @param question 用户问题
     * @return 生成的SQL语句
     */
    public String generateSql(String question) {
        // 获取数据库表结构描述
        String schemaDescription = dbSchemaService.generateSchemaDescription();

        // 构建Prompt
        String prompt = buildTextToSqlPrompt(question, schemaDescription);

        // 调用LLM生成SQL
        String generatedSql = chatModel.chat(prompt);

        // 清理SQL（去除可能的markdown代码块标记）
        return cleanSql(generatedSql);
    }

    /**
     * 构建Text-to-SQL的Prompt
     *
     * @param question          用户问题
     * @param schemaDescription 数据库表结构描述
     * @return 完整的Prompt
     */
    private String buildTextToSqlPrompt(String question, String schemaDescription) {
        return """
                你是一个SQL专家。请根据用户的问题和数据库表结构，生成对应的SQL查询语句。

                【数据库表结构】
                %s

                【示例】
                以下是一些问题和对应的标准SQL查询示例，请参考这些示例的风格和格式来编写SQL：

                示例1:
                问题: 查询所有订单
                SQL: SELECT id, order_no, status, department, target_location, contact_name, create_time FROM logistics_order ORDER BY create_time DESC

                示例2:
                问题: 查询待接单的订单
                SQL: SELECT id, order_no, order_type, department, target_location, contact_name, create_time FROM logistics_order WHERE status = 'pending' ORDER BY create_time DESC

                示例3:
                问题: 查询订单号ORD-20241201的详细信息
                SQL: SELECT o.id, o.order_no, o.order_type, o.status, o.department, o.target_location, o.contact_name, o.delivery_type, o.urgent, i.item_name, i.specification, i.quantity FROM logistics_order o LEFT JOIN logistics_order_item i ON o.id = i.order_id WHERE o.order_no = 'ORD-20241201'

                示例4:
                问题: 查询手术物资类型的紧急订单
                SQL: SELECT id, order_no, order_type, status, urgent, contact_name, create_time FROM logistics_order WHERE order_type = 'surgery_material' AND urgent = 1 ORDER BY create_time DESC

                示例5:
                问题: 查询张三的所有订单
                SQL: SELECT id, order_no, order_type, status, target_location, create_time FROM logistics_order WHERE contact_name = '张三' ORDER BY create_time DESC

                【重要约束】
                1. 只生成SELECT查询语句（禁止INSERT、UPDATE、DELETE）
                2. 订单表使用 logistics_order，别名 o
                3. 订单明细表使用 logistics_order_item，别名 i
                4. 两个表通过 order_id 关联（logistics_order_item.order_id = logistics_order.id）
                5. 返回的SQL必须可以直接在MySQL中执行
                6. 只返回SQL语句，不要包含任何解释性文字
                7. 注意字段类型的匹配（字符串用引号，数字不需要）
                8. 如果需要关联查询订单明细，请使用JOIN
                9. 状态值使用英文：pending / in_progress / completed / cancelled

                【用户问题】
                %s

                【生成的SQL】
                """.formatted(schemaDescription, question);
    }

    /**
     * 清理生成的SQL
     * 去除markdown代码块标记等，保留完整的多行SQL语句
     *
     * @param sql 原始SQL
     * @return 清理后的SQL
     */
    private String cleanSql(String sql) {
        if (sql == null || sql.isBlank()) {
            return "";
        }

        // 去除markdown代码块标记（支持 sql/java 等语言标识符）
        String cleaned = sql.replaceAll("```\\w*\\s*", "").replaceAll("```", "").trim();

        // 查找SELECT关键字起始位置，截取有效SQL部分
        int selectIdx = cleaned.toUpperCase().indexOf("SELECT");
        if (selectIdx >= 0) {
            cleaned = cleaned.substring(selectIdx).trim();
        }

        // 截取到最后一个分号（去除末尾可能的额外文字）
        int lastSemicolon = cleaned.lastIndexOf(';');
        if (lastSemicolon > 0) {
            cleaned = cleaned.substring(0, lastSemicolon + 1).trim();
        } else if (!cleaned.isEmpty() && !cleaned.endsWith(";") && cleaned.toUpperCase().startsWith("SELECT")) {
            // 没有分号但以SELECT开头，说明是完整SQL，直接返回
        }

        return cleaned;
    }

    /**
     * 验证生成的SQL是否安全
     * 只允许SELECT查询
     *
     * @param sql 待验证的SQL
     * @return 是否安全
     */
    public boolean isSqlSafe(String sql) {
        if (sql == null || sql.isBlank()) {
            return false;
        }

        String upperSql = sql.toUpperCase().trim();

        // 只允许SELECT查询
        if (!upperSql.startsWith("SELECT")) {
            log.warn("检测到非SELECT查询: {}", sql);
            return false;
        }

        // 禁止危险关键字
        String[] forbiddenKeywords = {"DROP", "DELETE", "UPDATE", "INSERT", "TRUNCATE", "ALTER", "CREATE"};
        for (String keyword : forbiddenKeywords) {
            if (upperSql.contains(keyword)) {
                log.warn("检测到危险关键字: {}", keyword);
                return false;
            }
        }

        return true;
    }
}
