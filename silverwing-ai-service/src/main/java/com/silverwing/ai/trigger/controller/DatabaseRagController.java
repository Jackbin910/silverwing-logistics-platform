package com.silverwing.ai.trigger.controller;

import com.silverwing.ai.application.rag.DatabaseRagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Database RAG Controller
 * 提供基于数据库的智能问答接口
 */
@Slf4j
@RestController
@RequestMapping("/database-rag")
@RequiredArgsConstructor
@Tag(name = "Database RAG", description = "数据库智能问答接口")
public class DatabaseRagController {

    private final DatabaseRagService databaseRagService;

    /**
     * 智能问答接口
     * 用户通过自然语言查询订单数据
     *
     * @return 回答结果
     */
    @PostMapping("/query")
    @Operation(summary = "智能问答", description = "通过自然语言查询订单数据")
    public Map<String, Object> query(@RequestBody Map<String, String> request) {
        String question = request.get("question");

        if (question == null || question.isBlank()) {
            return buildErrorResponse("问题不能为空");
        }

        try {
            log.info("收到Database RAG查询: {}", question);
            String answer = databaseRagService.query(question);
            return buildSuccessResponse(answer);
        } catch (Exception e) {
            log.error("Database RAG查询失败", e);
            return buildErrorResponse("处理失败: " + e.getMessage());
        }
    }

    /**
     * GET方式的智能问答接口
     *
     * @param question 用户问题
     * @return 回答结果
     */
    @GetMapping("/query")
    @Operation(summary = "智能问答(GET)", description = "通过自然语言查询订单数据")
    public Map<String, Object> queryGet(@RequestParam String question) {
        if (question == null || question.isBlank()) {
            return buildErrorResponse("问题不能为空");
        }

        try {
            log.info("收到Database RAG查询: {}", question);
            String answer = databaseRagService.query(question);
            return buildSuccessResponse(answer);
        } catch (Exception e) {
            log.error("Database RAG查询失败", e);
            return buildErrorResponse("处理失败: " + e.getMessage());
        }
    }

    /**
     * 获取数据库表结构信息
     *
     * @return 表结构信息
     */
    @GetMapping("/schema")
    @Operation(summary = "获取表结构", description = "获取可查询的数据库表结构信息")
    public Map<String, Object> getSchema() {
        try {
            String schema = databaseRagService.getSchemaInfo();
            return buildSuccessResponse(schema);
        } catch (Exception e) {
            log.error("获取表结构失败", e);
            return buildErrorResponse("获取失败: " + e.getMessage());
        }
    }

    /**
     * 示例问题接口
     *
     * @return 可用的示例问题
     */
    @GetMapping("/examples")
    @Operation(summary = "示例问题", description = "获取可用的示例问题")
    public Map<String, Object> getExamples() {
        Map<String, Object> examples = new HashMap<>();
        examples.put("questions", new String[]{
                "查询所有订单",
                "查询待接单的订单",
                "查询订单编号为 ORD20240315001 的订单明细",
                "查询手术物资类型的订单",
                "查询今天创建的订单",
                "查询紧急的订单",
                "查询目标位置包含三楼的订单",
                "查询张三的订单"
        });
        return buildSuccessResponse(examples);
    }

    /**
     * 构建成功响应
     */
    private Map<String, Object> buildSuccessResponse(Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);
        return response;
    }

    /**
     * 构建错误响应
     */
    private Map<String, Object> buildErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        return response;
    }
}
