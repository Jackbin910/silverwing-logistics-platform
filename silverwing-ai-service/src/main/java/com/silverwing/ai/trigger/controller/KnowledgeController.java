package com.silverwing.ai.trigger.controller;

import com.silverwing.ai.application.dto.KnowledgeIngestResult;
import com.silverwing.ai.application.rag.KnowledgeIngestService;
import com.silverwing.ai.application.rag.KnowledgeQaService;
import com.silverwing.common.annotation.Log;
import com.silverwing.common.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 知识库管理 Controller
 * 提供知识文档导入（支持 PDF / Word / Markdown 文件）、问答、删除等 REST API
 */
@Tag(name = "知识库管理", description = "RAG 知识库文档管理、智能问答")
@RestController
@RequestMapping("/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeIngestService ingestService;

    private final KnowledgeQaService qaService;

    /**
     * 上传文档文件并导入知识库
     * 支持 PDF / Word（doc、docx）/ Markdown（md）等格式
     */
    @Log(title = "知识库-导入文档", businessType = 1, saveResult = false)
    @Operation(summary = "导入文档", description = "上传文档文件，自动解析、切分、向量化后存入知识库")
    @PostMapping(value = "/ingest", consumes = "multipart/form-data")
    public Result<KnowledgeIngestResult> ingest(
            @Parameter(description = "文档文件", required = true)
            @RequestPart("file") MultipartFile file,
            @Parameter(description = "文档标题（可选，默认使用文件名）")
            @RequestParam(value = "title", required = false) String title) {

        if (file == null || file.isEmpty()) {
            return Result.fail("文件不能为空");
        }

        KnowledgeIngestResult result = ingestService.ingest(title, file);
        return Result.success(result);
    }

    /**
     * 基于知识库的智能问答
     */
    @Operation(summary = "知识库问答", description = "基于向量相似度检索知识库并生成回答")
    @PostMapping("/qa")
    public Result<Map<String, String>> qa(
            @Parameter(description = "问题内容", required = true)
            @RequestBody String question) {

        if (question == null || question.isBlank()) {
            return Result.fail("问题不能为空");
        }

        String answer = qaService.answer(question);
        return Result.success(Map.of("answer", answer));
    }

    /**
     * 根据文档 ID 删除知识库中的分片
     */
    @Log(title = "知识库-删除文档", businessType = 3)
    @Operation(summary = "删除文档", description = "根据文档 ID 删除知识库中该文档的所有分片")
    @DeleteMapping("/delete/{documentId}")
    public Result<String> deleteByDocumentId(@PathVariable("documentId") String documentId) {
        ingestService.deleteByDocumentId(documentId);
        return Result.success("已删除文档: " + documentId);
    }

    /**
     * 清空整个知识库
     */
    @Log(title = "知识库-清空知识库", businessType = 3)
    @Operation(summary = "清空知识库", description = "清空知识库中所有向量数据，谨慎操作！")
    @DeleteMapping("/clear")
    public Result<String> clearAll() {
        ingestService.clearAll();
        return Result.success("知识库已清空");
    }
}
