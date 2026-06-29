package com.silverwing.ai.controller;

import com.silverwing.ai.domain.dto.KnowledgeIngestRequest;
import com.silverwing.ai.domain.dto.KnowledgeIngestResult;
import com.silverwing.ai.service.rag.KnowledgeIngestService;
import com.silverwing.ai.service.rag.KnowledgeQaService;
import com.silverwing.common.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.data.segment.TextSegment;

import java.util.Map;

/**
 * 知识库管理 Controller
 * 提供知识文档导入、问答、删除等 REST API
 */
@Tag(name = "知识库管理", description = "RAG 知识库文档管理、智能问答")
@RestController
@RequestMapping("/knowledge")
@RequiredArgsConstructor
@ConditionalOnBean(EmbeddingStore.class)
public class KnowledgeController {

    private final KnowledgeIngestService ingestService;

    private final KnowledgeQaService qaService;

    /**
     * 导入文本文档到知识库
     */
    @Operation(summary = "导入文档", description = "将文本文档切分、向量化后存入知识库")
    @PostMapping("/ingest")
    public Result<KnowledgeIngestResult> ingest(@Valid @RequestBody KnowledgeIngestRequest request) {
        KnowledgeIngestResult result = ingestService.ingest(request);
        return Result.success(result);
    }

    /**
     * 基于知识库的智能问答
     */
    @Operation(summary = "知识库问答", description = "基于向量相似度检索知识库并生成回答")
    @PostMapping("/qa")
    public Result<Map<String, String>> qa(
            @RequestBody Map<String, String> body) {
        String question = body.get("question");
        String warehouseId = body.get("warehouseId");
        String deviceType = body.get("deviceType");

        if (question == null || question.isBlank()) {
            return Result.fail("问题不能为空");
        }

        String answer = qaService.answer(question, warehouseId, deviceType);
        return Result.success(Map.of("answer", answer));
    }

    /**
     * 根据文档 ID 删除知识库中的分片
     */
    @Operation(summary = "删除文档", description = "根据文档 ID 删除知识库中该文档的所有分片")
    @DeleteMapping
    public Result<String> deleteByDocumentId(
            @Parameter(description = "文档 ID") @RequestParam String documentId) {
        ingestService.deleteByDocumentId(documentId);
        return Result.success("已删除文档: " + documentId);
    }

    /**
     * 清空整个知识库
     */
    @Operation(summary = "清空知识库", description = "清空知识库中所有向量数据，谨慎操作！")
    @DeleteMapping("/clear")
    public Result<String> clearAll() {
        ingestService.clearAll();
        return Result.success("知识库已清空");
    }
}
