package com.silverwing.ai.domain.service.rag;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.regex.Pattern;

/**
 * 文档解析组件
 * 基于 LangChain4j 的 ApacheTikaDocumentParser 自动识别并解析 PDF / Word / Markdown 等多种格式文档，提取纯文本内容
 * <p>
 * Tika 对 Word/PDF 的段落结构提取不稳定，常把标题、列表、表格挤成一整段。
 * 解析后统一做结构归一化，把高置信度的结构化标记恢复成独占行，
 * 使后续切分能按语义边界进行，LLM 也能拿到自带段落结构的上下文。
 * </p>
 */
@Slf4j
@Component
public class DocumentParser {

    /**
     * 结构化标记：命中时在其前面补换行，恢复文档层级。
     * 用捕获组保留标记本身、不捕获前导空白；(?&lt;!\\n) 保证对已换行的文本幂等。
     * 仅识别高置信度标记，宁可少识别也不误伤正文。
     */
    private static final Pattern[] STRUCTURE_PATTERNS = {
            // Markdown 标题（含无空格写法，如「###1.适用范围」）
            Pattern.compile("(?<!\\n)[ \\t]*(#{1,6}[ \\t]*)"),
            // 中文序号标题：一、/（一）
            Pattern.compile("(?<!\\n)[ \\t]*([（(]?[一二三四五六七八九十百]+[)）]?[、.．])"),
            // 「第三章」这类章节标题
            Pattern.compile("(?<!\\n)[ \\t]*(第[一二三四五六七八九十百\\d]+[章节篇条部])"),
            // 表格数据行
            Pattern.compile("(?<!\\n)[ \\t]*(\\|)"),
            // 无序列表项
            Pattern.compile("(?<!\\n)[ \\t]*([-•·*](?=[ \\t]))"),
            // 数字步骤（1. / 1、后跟中文或空白，避开小数）
            Pattern.compile("(?<!\\n)[ \\t]*(\\d{1,2}[.．、](?=[\\u4e00-\\u9fa5 \\t]))"),
            // 标签：异常1：/ 注意：/ 口诀：
            Pattern.compile("(?<!\\n)[ \\t]*((?:异常\\d+|注意|说明|口诀|提示)[：:])")
    };

    /**
     * 解析上传的文档文件，提取纯文本内容
     *
     * @param file 上传的文档文件（支持 PDF / Word / Markdown 等格式）
     * @return 解析并恢复结构后的纯文本
     */
    public String parse(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        String fileName = file.getOriginalFilename();
        log.info("开始解析文档: fileName={}, size={}", fileName, file.getSize());

        // LangChain4j 提供的 ApacheTikaDocumentParser，内部封装 Apache Tika 自动检测文件格式
        ApacheTikaDocumentParser parser = new ApacheTikaDocumentParser();

        try (InputStream stream = file.getInputStream()) {
            Document document = parser.parse(stream);

            if (document == null) {
                throw new IllegalStateException("文档内容为空，无法提取文本");
            }

            String content = document.text();
            if (content == null || content.isBlank()) {
                throw new IllegalStateException("文档内容为空，无法提取文本");
            }

            String normalized = normalizeStructure(content);
            log.info("文档解析成功: fileName={}, 提取字符数={}, 结构归一化后={}",
                    fileName, content.length(), normalized.length());
            return normalized;
        } catch (Exception e) {
            log.error("文档解析失败: fileName={}", fileName, e);
            throw new RuntimeException("文档解析失败: " + e.getMessage(), e);
        }
    }

    /**
     * 恢复被 Tika 压平的结构，把标题/列表/表格/步骤等标记还原成独占行。
     * <p>
     * 逐个结构化标记在其前面补一个换行（已有换行则不动），最后压缩多余空行、
     * 去掉行尾空白。此步骤是切分质量的关键：只有文本本身带了正确换行，
     * 后续的按空行/按行递归切分才能落在语义边界上。
     * </p>
     *
     * @param text Tika 提取的原始纯文本
     * @return 恢复结构后的文本
     */
    private String normalizeStructure(String text) {
        String normalized = text;
        for (Pattern pattern : STRUCTURE_PATTERNS) {
            // $1 保留标记本身，前面统一补换行；(?<!\n) 保证不重复插入
            normalized = pattern.matcher(normalized).replaceAll("\n$1");
        }
        // 连续换行压缩为最多两个（保留段落间空行），并清理行尾空白
        normalized = normalized.replaceAll("\\n{3,}", "\n\n")
                .replaceAll("[ \\t]+\\n", "\n");
        return normalized.trim();
    }

    /**
     * 从文件名中提取扩展名（小写，不含点）
     *
     * @param fileName 原始文件名
     * @return 扩展名，无扩展名时返回空字符串
     */
    public String extractExtension(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "";
        }
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dotIndex + 1).toLowerCase();
    }
}
