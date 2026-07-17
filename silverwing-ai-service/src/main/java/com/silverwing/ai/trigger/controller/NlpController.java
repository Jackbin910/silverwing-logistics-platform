package com.silverwing.ai.trigger.controller;

import com.silverwing.ai.application.dto.*;
import com.silverwing.common.domain.Result;
import com.silverwing.ai.application.NlpService;
import com.silverwing.ai.application.impl.ConversationOrchestrator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import com.silverwing.ai.domain.model.IntentResult;
import com.silverwing.ai.domain.model.ClassifyResult;
import com.silverwing.ai.domain.model.SentimentResult;
import com.silverwing.ai.domain.model.SummaryResult;
import com.silverwing.ai.domain.model.NlpAnalyzeResult;

/**
 * NLP 自然语言处理 Controller
 * 提供意图识别、实体提取、文本分类、情感分析、文本摘要等 REST 接口
 */
@Tag(name = "NLP自然语言处理", description = "意图识别、实体提取、文本分类、情感分析、文本摘要")
@RestController
@RequestMapping("/nlp")
@RequiredArgsConstructor
public class NlpController {

    private final NlpService nlpService;
    private final ConversationOrchestrator conversationOrchestrator;

    /**
     * 智能对话（核心接口）
     * 接收自然语言输入，经过 NLP 解析、业务查询后返回自然语言回答
     */
    @Operation(summary = "智能对话", description = "自然语言交互入口，自动识别意图并查询业务数据返回回答")
    @PostMapping("/chat")
    public Result<ConversationResponse> chat(@Valid @RequestBody ChatRequest request) {
        ConversationResponse response = conversationOrchestrator.chat(request.getMessage());
        return Result.success(response);
    }

    /**
     * 意图识别
     */
    @Operation(summary = "意图识别", description = "识别用户输入的意图类型")
    @PostMapping("/intent")
    public Result<IntentResult> recognizeIntent(@Valid @RequestBody TextRequest request) {
        IntentResult result = nlpService.recognizeIntent(request.getText());
        return Result.success(result);
    }

    /**
     * 命名实体提取
     */
    @Operation(summary = "实体提取", description = "从文本中提取设备编码、仓库名、订单号等关键实体")
    @PostMapping("/entity")
    public Result<Map<String, Object>> extractEntities(@Valid @RequestBody TextRequest request) {
        var entities = nlpService.extractEntities(request.getText());
        Map<String, Object> result = new HashMap<>();
        result.put("entities", entities);
        result.put("count", entities.size());
        return Result.success(result);
    }

    /**
     * 文本分类
     */
    @Operation(summary = "文本分类", description = "对工单、告警等文本进行自动分类")
    @PostMapping("/classify")
    public Result<ClassifyResult> classifyText(@Valid @RequestBody TextRequest request) {
        ClassifyResult result = nlpService.classifyText(request.getText());
        return Result.success(result);
    }

    /**
     * 情感分析
     */
    @Operation(summary = "情感分析", description = "分析文本的情感倾向（正面/中性/负面）")
    @PostMapping("/sentiment")
    public Result<SentimentResult> analyzeSentiment(@Valid @RequestBody TextRequest request) {
        SentimentResult result = nlpService.analyzeSentiment(request.getText());
        return Result.success(result);
    }

    /**
     * 文本摘要
     */
    @Operation(summary = "文本摘要", description = "对长文本生成简洁摘要")
    @PostMapping("/summary")
    public Result<SummaryResult> summarizeText(@Valid @RequestBody TextRequest request) {
        SummaryResult result = nlpService.summarizeText(request.getText());
        return Result.success(result);
    }

    /**
     * 综合分析（一次调用完成意图 + 实体 + 分类 + 情感）
     */
    @Operation(summary = "综合分析", description = "一次调用完成意图识别、实体提取、文本分类和情感分析")
    @PostMapping("/analyze")
    public Result<NlpAnalyzeResult> analyze(@Valid @RequestBody TextRequest request) {
        NlpAnalyzeResult result = nlpService.analyze(request.getText());
        return Result.success(result);
    }
}
