package com.silverwing.ai.domain.service.rag;

import cn.hutool.core.util.IdUtil;
import com.silverwing.ai.application.convertor.AiConvertor;
import com.silverwing.ai.application.dto.KnowledgeDocumentDTO;
import com.silverwing.ai.application.dto.KnowledgeIngestResult;
import com.silverwing.biz.ai.domain.entity.KnowledgeDocumentAggregate;
import com.silverwing.biz.ai.domain.repository.KnowledgeDocumentRepository;
import com.silverwing.common.domain.PageRequest;
import com.silverwing.common.domain.PageResult;
import com.silverwing.common.storage.core.FileStorageService;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.filter.Filter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey;

/**
 * 知识库导入服务
 * 负责将上传的文档文件（PDF / Word / Markdown 等）解析、切分、向量化并存入 PGVector 向量数据库
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeIngestService {

    /**
     * 默认分片最大字符数（中文约 500 字/段）
     */
    private static final int MAX_CHUNK_SIZE = 500;

    /**
     * 默认分片重叠字符数（保证上下文连续性）
     */
    private static final int OVERLAP_SIZE = 50;

    private final EmbeddingModel embeddingModel;

    private final EmbeddingStore<TextSegment> embeddingStore;

    private final KnowledgeDocumentRepository documentRepository;

    private final DocumentParser documentParser;

    private final AiConvertor aiConvertor;

    /**
     * 对象存储服务（可选）：未启用 silverwing.storage.enabled 时为空，不影响原有导入流程
     */
    private final ObjectProvider<FileStorageService> storageProvider;

    /**
     * 导入知识库文档（带元信息记录）
     * 自动解析上传的文件，提取纯文本后切分、向量化、存入向量数据库
     *
     * @param title 文档标题（为空时使用文件名）
     * @param file 上传的文档文件（支持 PDF / Word / Markdown）
     * @return 导入结果
     */
    @Transactional(rollbackFor = Exception.class)
    public KnowledgeIngestResult ingest(String title, MultipartFile file) {
        // 标题为空时使用文件名（去掉扩展名）
        String fileName = file.getOriginalFilename();
        if (title == null || title.isBlank()) {
            title = stripExtension(fileName);
        }

        // 0. 先持久化原始文件到对象存储（RustFS），记录 Key / URL
        //    解析前落盘，便于原文档追溯与后续重新向量化；未启用存储时自动跳过
        String fileKey = null;
        String fileUrl = null;
        FileStorageService storage = storageProvider.getIfAvailable();
        fileKey = storage.upload(file, "rag");
        fileUrl = storage.getFileUrl(fileKey);


        // 1. 解析文件，提取纯文本
        String content = documentParser.parse(file);
        String fileType = documentParser.extractExtension(fileName);
        long fileSize = file.getSize();

        // 2. 生成文档ID
        String documentId = IdUtil.fastSimpleUUID();

        // 3. 初始化文档记录（待处理状态，写入 fileKey / fileUrl）
        KnowledgeDocumentAggregate doc = new KnowledgeDocumentAggregate();
        doc.setDocumentId(documentId);
        doc.setTitle(title);
        doc.setFileName(fileName);
        doc.setFileType(fileType);
        doc.setFileSize(fileSize);
        doc.setWordCount(content.length());
        doc.setFileKey(fileKey);
        doc.setFileUrl(fileUrl);
        doc.setStatus(0);
        documentRepository.insert(doc);

        try {
            // 4. 执行向量导入（documentId 写入 metadata，便于后续按文档删除向量）
            int chunkCount = ingest(title, content, documentId);

            // 5. 更新文档状态为已导入
            doc.setChunkCount(chunkCount);
            doc.setStatus(1); // 已导入
            documentRepository.updateById(doc);

            return KnowledgeIngestResult.builder()
                    .documentId(documentId)
                    .title(title)
                    .chunkCount(chunkCount)
                    .wordCount(content.length())
                    .status("SUCCESS")
                    .message("文档导入成功")
                    .fileKey(fileKey)
                    .fileUrl(fileUrl)
                    .build();

        } catch (Exception e) {
            // 向量化失败时清理已上传的原件，避免孤儿对象
            if (fileKey != null) {
                try {
                    storage.deleteFile(fileKey);
                } catch (Exception ignore) {
                    log.warn("导入失败清理原始文件失败: fileKey={}", fileKey, ignore);
                }
            }
            // 更新文档状态为导入失败
            doc.setStatus(2);
            doc.setErrorMsg(e.getMessage());
            documentRepository.updateById(doc);
            throw new RuntimeException("知识库导入失败: " + e.getMessage(), e);
        }
    }

    /**
     * 导入纯文本文档到知识库
     * 自动按段落切分、向量化、存入向量数据库
     * documentId 会写入每个分片的 metadata，用于后续按文档 ID 精确删除向量
     *
     * @param title      文档标题
     * @param content    文档纯文本内容
     * @param documentId 文档唯一标识（写入 metadata）
     * @return 导入的分片数量
     */
    public int ingest(String title, String content, String documentId) {
        try {
            // 1. 构建文档元信息（documentId 用于后续按文档精确删除向量）
            Metadata metadata = Metadata.from("title", title);
            metadata.put("documentId", documentId);

            // 2. 按段落切分文本
            List<TextSegment> chunks = splitIntoChunks(content, metadata);
            log.info("文档切分完成: title={}, documentId={}, 切分后分片数={}", title, documentId, chunks.size());

            // 3. 批量向量化并存入向量数据库
            List<Embedding> embeddings = embeddingModel.embedAll(chunks).content();

            embeddingStore.addAll(embeddings, chunks);

            log.info("知识库导入成功: title={}, documentId={}, chunkCount={}", title, documentId, chunks.size());
            return chunks.size();

        } catch (Exception e) {
            log.error("知识库导入失败: title={}, documentId={}", title, documentId, e);
            throw new RuntimeException("知识库导入失败: " + e.getMessage(), e);
        }
    }

    /**
     * 将文本按段落切分成小段（TextSegment）
     * 策略：先按空行分段，再按最大长度合并/拆分，段间保留重叠
     *
     * @param text     原始文本
     * @param metadata 元信息（会复制到每个分片）
     * @return 切分后的 TextSegment 列表
     */
    private List<TextSegment> splitIntoChunks(String text, Metadata metadata) {
        List<TextSegment> chunks = new ArrayList<>();

        // 按空行（连续两个以上换行符）分割段落
        String[] paragraphs = text.split("\\n{2,}");

        StringBuilder currentChunk = new StringBuilder();
        int chunkIndex = 0;

        for (String paragraph : paragraphs) {
            String trimmed = paragraph.trim();
            if (trimmed.isEmpty()) {
                continue;
            }

            // 当前块加上新段落后超过最大长度，先保存当前块
            if (currentChunk.length() > 0
                    && currentChunk.length() + trimmed.length() + 1 > MAX_CHUNK_SIZE) {
                chunks.add(createChunk(currentChunk.toString(), metadata, chunkIndex++));

                // 保留尾部作为重叠
                String overlap = extractOverlap(currentChunk.toString());
                currentChunk = new StringBuilder(overlap);
            }

            // 单个段落超过最大长度，需要按句子/换行进一步拆分
            if (trimmed.length() > MAX_CHUNK_SIZE) {
                // 先保存当前积累的内容
                if (currentChunk.length() > 0) {
                    chunks.add(createChunk(currentChunk.toString(), metadata, chunkIndex++));
                    currentChunk = new StringBuilder();
                }
                // 按换行拆分长段落
                String[] lines = trimmed.split("\\n");
                for (String line : lines) {
                    String lineTrimmed = line.trim();
                    if (lineTrimmed.isEmpty()) {
                        continue;
                    }
                    if (currentChunk.length() + lineTrimmed.length() + 1 > MAX_CHUNK_SIZE
                            && !currentChunk.isEmpty()) {
                        chunks.add(createChunk(currentChunk.toString(), metadata, chunkIndex++));
                        currentChunk = new StringBuilder();
                    }
                    if (!currentChunk.isEmpty()) {
                        currentChunk.append("\n");
                    }
                    currentChunk.append(lineTrimmed);
                }
            } else {
                if (!currentChunk.isEmpty()) {
                    currentChunk.append("\n\n");
                }
                currentChunk.append(trimmed);
            }
        }

        // 保存最后一个块
        if (!currentChunk.isEmpty()) {
            chunks.add(createChunk(currentChunk.toString(), metadata, chunkIndex));
        }

        return chunks;
    }

    /**
     * 创建 TextSegment（附带分片索引元信息）
     *
     * @param text     文本内容
     * @param metadata 文档级元信息
     * @param index    分片索引
     * @return TextSegment 实例
     */
    private TextSegment createChunk(String text, Metadata metadata, int index) {
        Metadata chunkMetadata = metadata.copy();
        chunkMetadata.put("index", index);
        return TextSegment.from(text.trim(), chunkMetadata);
    }

    /**
     * 提取文本尾部作为重叠部分
     *
     * @param text 原始文本
     * @return 尾部重叠文本
     */
    private String extractOverlap(String text) {
        if (text.length() <= OVERLAP_SIZE) {
            return text;
        }
        // 从最后一个换行处截断，避免截断到句子中间
        int cutPoint = text.lastIndexOf('\n', text.length() - OVERLAP_SIZE);
        if (cutPoint < 0 || text.length() - cutPoint > OVERLAP_SIZE * 2) {
            // 找不到合适的换行符，直接从字符位置截断
            return text.substring(text.length() - OVERLAP_SIZE);
        }
        return text.substring(cutPoint + 1);
    }

    /**
     * 清空知识库中所有向量数据
     */
    @Transactional(rollbackFor = Exception.class)
    public void clearAll() {
        try {
            // 清空向量库
            embeddingStore.removeAll();
            // 清空MySQL文档记录
            documentRepository.deleteAll();
            log.info("知识库已清空");
        } catch (Exception e) {
            log.error("清空知识库失败", e);
            throw new RuntimeException("清空知识库失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据文档ID删除知识库中的向量数据
     * 同时删除 MySQL 文档记录和 PGVector 中该文档的所有分片向量
     *
     * @param documentId 文档ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteByDocumentId(String documentId) {
        try {
            // 1. 按 documentId 过滤条件删除 PGVector 中的所有向量分片
            Filter filter = metadataKey("documentId").isEqualTo(documentId);
            embeddingStore.removeAll(filter);
            log.info("已删除向量数据: documentId={}", documentId);

            // 2. 逻辑删除 MySQL 中的文档记录
            documentRepository.deleteByDocumentId(documentId);
            log.info("已删除文档记录: documentId={}", documentId);
        } catch (Exception e) {
            log.error("删除文档失败: documentId={}", documentId, e);
            throw new RuntimeException("删除文档失败: " + e.getMessage(), e);
        }
    }

    /**
     * 分页查询知识库文档（供管理页面列表展示）
     *
     * @param page    分页请求（current/size）
     * @param keyword 标题关键词（可选）
     * @param status  文档状态（可选）
     * @return 分页后的文档 DTO
     */
    public PageResult<KnowledgeDocumentDTO> pageDocuments(PageRequest page, String keyword, Integer status) {
        try {
            page.normalize();
            PageResult<KnowledgeDocumentAggregate> aggregatePage =
                    documentRepository.pageDocuments(page.getCurrent(), page.getSize(), keyword, status);
            List<KnowledgeDocumentDTO> dtoList = aggregatePage.getRecords().stream()
                    .map(aiConvertor::toKnowledgeDocumentDto)
                    .collect(Collectors.toList());
            return new PageResult<>(aggregatePage.getCurrent(), aggregatePage.getSize(),
                    aggregatePage.getTotal(), dtoList);
        } catch (Exception e) {
            log.error("查询知识库文档列表失败: keyword={}, status={}", keyword, status, e);
            throw new RuntimeException("查询文档列表失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据文档ID查询文档详情
     *
     * @param documentId 文档唯一标识
     * @return 文档 DTO，不存在时返回 null
     */
    public KnowledgeDocumentDTO getDocument(String documentId) {
        try {
            KnowledgeDocumentAggregate aggregate = documentRepository.findByDocumentId(documentId);
            return aggregate == null ? null : aiConvertor.toKnowledgeDocumentDto(aggregate);
        } catch (Exception e) {
            log.error("查询知识库文档详情失败: documentId={}", documentId, e);
            throw new RuntimeException("查询文档详情失败: " + e.getMessage(), e);
        }
    }

    /**
     * 去掉文件名的扩展名
     *
     * @param fileName 原始文件名
     * @return 不含扩展名的文件名
     */
    private String stripExtension(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "未命名文档";
        }
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName;
    }
}
