package com.silverwing.ai.service.rag;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * 文档解析组件
 * 基于 LangChain4j 的 ApacheTikaDocumentParser 自动识别并解析 PDF / Word / Markdown 等多种格式文档，提取纯文本内容
 */
@Slf4j
@Component
public class DocumentParser {

    /**
     * 解析上传的文档文件，提取纯文本内容
     *
     * @param file 上传的文档文件（支持 PDF / Word / Markdown 等格式）
     * @return 解析后的纯文本
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

            log.info("文档解析成功: fileName={}, 提取字符数={}", fileName, content.length());
            return content.trim();
        } catch (Exception e) {
            log.error("文档解析失败: fileName={}", fileName, e);
            throw new RuntimeException("文档解析失败: " + e.getMessage(), e);
        }
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
