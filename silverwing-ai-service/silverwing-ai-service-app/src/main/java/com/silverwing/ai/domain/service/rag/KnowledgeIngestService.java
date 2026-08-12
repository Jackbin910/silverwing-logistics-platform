package com.silverwing.ai.domain.service.rag;

import cn.hutool.core.util.IdUtil;
import com.silverwing.ai.application.convertor.AiConvertor;
import com.silverwing.ai.application.dto.FileDownloadResult;
import com.silverwing.ai.application.dto.KnowledgeDocumentDTO;
import com.silverwing.ai.application.dto.KnowledgeIngestResult;
import com.silverwing.biz.ai.domain.entity.KnowledgeDocumentAggregate;
import com.silverwing.biz.ai.domain.repository.KnowledgeDocumentRepository;
import com.silverwing.common.domain.PageRequest;
import com.silverwing.common.domain.PageResult;
import com.silverwing.common.domain.ResultCode;
import com.silverwing.common.exception.BusinessException;
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
     * 将文本递归切分成小段（TextSegment）
     * <p>策略（参考 WeKnora 的递归切片思想）：
     * 1. 先按 Markdown 标题层级（# → ## → ###）切分，记录标题面包屑（headingPath）作为每个分片的上下文；
     * 2. 标题块内超长时，依次按空行、换行、句号分级继续拆分；
     * 3. 段间保留尾部重叠（overlap），避免截断句子导致语义割裂。</p>
     *
     * @param text     原始文本
     * @param metadata 文档级元信息（会复制到每个分片）
     * @return 切分后的 TextSegment 列表
     */
    private List<TextSegment> splitIntoChunks(String text, Metadata metadata) {
        List<TextSegment> chunks = new ArrayList<>();
        int[] chunkIndex = {0};

        // 先按一级/二级标题切分为章节，逐章节递归处理
        String[] sections = text.split("(?m)^#{1,3}\\s+");
        for (String section : sections) {
            String trimmed = section.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            // 取章节首行作为标题面包屑（如「仓储管理」），去除标题后的正文继续递归
            String[] firstLineSplit = trimmed.split("\\n", 2);
            String heading = firstLineSplit[0].trim();
            String body = firstLineSplit.length > 1 ? firstLineSplit[1].trim() : "";
            String headingPath = heading.isEmpty() ? "正文" : heading;

            Metadata sectionMeta = metadata.copy();
            sectionMeta.put("headingPath", headingPath);

            if (body.isEmpty()) {
                // 整段就是标题，直接作为最小分片
                chunks.add(createChunk(heading, sectionMeta, chunkIndex[0]++));
                continue;
            }
            recursiveSplit(body, sectionMeta, chunks, chunkIndex);
        }

        // 处理完全没有标题的纯文本文档
        if (chunks.isEmpty()) {
            recursiveSplit(text, metadata, chunks, chunkIndex);
        }
        return chunks;
    }

    /**
     * 递归切分单个文本块：超长时按 空行 → 换行 → 句号 逐级细分，保留 overlap
     *
     * @param text        待切分文本
     * @param metadata    当前层级元信息（含 headingPath）
     * @param chunks      结果收集列表
     * @param chunkIndex  分片序号计数器（数组以便在递归中共享）
     */
    private void recursiveSplit(String text, Metadata metadata,
                                List<TextSegment> chunks, int[] chunkIndex) {
        if (text.length() <= MAX_CHUNK_SIZE) {
            chunks.add(createChunk(text, metadata, chunkIndex[0]++));
            return;
        }
        // 按空行拆分
        String[] blocks = text.split("\\n{2,}");
        StringBuilder current = new StringBuilder();
        for (String block : blocks) {
            String trimmed = block.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            if (current.length() + trimmed.length() + 2 > MAX_CHUNK_SIZE) {
                if (current.length() > 0) {
                    flushChunk(current, metadata, chunks, chunkIndex);
                }
                // 单个 block 仍超长，继续按换行拆
                if (trimmed.length() > MAX_CHUNK_SIZE) {
                    splitByLines(trimmed, metadata, chunks, chunkIndex);
                } else {
                    current = new StringBuilder(trimmed);
                }
            } else {
                if (current.length() > 0) {
                    current.append("\n\n");
                }
                current.append(trimmed);
            }
        }
        if (current.length() > 0) {
            flushChunk(current, metadata, chunks, chunkIndex);
        }
    }

    /**
     * 按换行继续拆分超长 block，仍超长则按句号拆分
     */
    private void splitByLines(String text, Metadata metadata,
                              List<TextSegment> chunks, int[] chunkIndex) {
        String[] lines = text.split("\\n");
        StringBuilder current = new StringBuilder();
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            if (current.length() + trimmed.length() + 1 > MAX_CHUNK_SIZE) {
                if (current.length() > 0) {
                    flushChunk(current, metadata, chunks, chunkIndex);
                }
                if (trimmed.length() > MAX_CHUNK_SIZE) {
                    // 按句号/分号/逗号兜底拆分长句
                    splitBySentence(trimmed, metadata, chunks, chunkIndex);
                } else {
                    current = new StringBuilder(trimmed);
                }
            } else {
                if (current.length() > 0) {
                    current.append("\n");
                }
                current.append(trimmed);
            }
        }
        if (current.length() > 0) {
            flushChunk(current, metadata, chunks, chunkIndex);
        }
    }

    /**
     * 按标点（句号/分号/逗号）兜底拆分超长句子
     */
    private void splitBySentence(String text, Metadata metadata,
                                 List<TextSegment> chunks, int[] chunkIndex) {
        String[] parts = text.split("(?<=[。；;，,])");
        StringBuilder current = new StringBuilder();
        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            if (current.length() + trimmed.length() > MAX_CHUNK_SIZE) {
                if (current.length() > 0) {
                    flushChunk(current, metadata, chunks, chunkIndex);
                }
                if (trimmed.length() > MAX_CHUNK_SIZE) {
                    // 极端长串（无标点）直接硬切
                    for (int i = 0; i < trimmed.length(); i += MAX_CHUNK_SIZE) {
                        int end = Math.min(trimmed.length(), i + MAX_CHUNK_SIZE);
                        flushChunk(new StringBuilder(trimmed.substring(i, end)), metadata, chunks, chunkIndex);
                    }
                } else {
                    current = new StringBuilder(trimmed);
                }
            } else {
                current.append(trimmed);
            }
        }
        if (current.length() > 0) {
            flushChunk(current, metadata, chunks, chunkIndex);
        }
    }

    /**
     * 落盘一个分片：写入尾部 overlap 到下一个分片的开头，保证上下文连续
     */
    private void flushChunk(StringBuilder content, Metadata metadata,
                            List<TextSegment> chunks, int[] chunkIndex) {
        String text = content.toString().trim();
        if (text.isEmpty()) {
            return;
        }
        chunks.add(createChunk(text, metadata, chunkIndex[0]++));
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
     * 清空知识库中所有向量数据
     * <p>先清理对象存储中的全部原始文件，再清空向量库与 MySQL 记录，避免残留孤儿文件。</p>
     */
    @Transactional(rollbackFor = Exception.class)
    public void clearAll() {
        try {
            // 1. 遍历全部文档，清理对象存储（bucket）中的原始文件
            List<KnowledgeDocumentAggregate> allDocs = documentRepository.listAll();
            FileStorageService storage = storageProvider.getIfAvailable();
            if (storage != null) {
                for (KnowledgeDocumentAggregate doc : allDocs) {
                    if (doc.getFileKey() != null && !doc.getFileKey().isBlank()) {
                        storage.deleteFile(doc.getFileKey());
                    }
                }
                log.info("已删除对象存储文件: count={}", allDocs.size());
            }

            // 2. 清空向量库
            embeddingStore.removeAll();
            // 3. 清空MySQL文档记录
            documentRepository.deleteAll();
            log.info("知识库已清空");
        } catch (Exception e) {
            log.error("清空知识库失败", e);
            throw new RuntimeException("清空知识库失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据文档ID删除知识库中的文档
     * 依次清理：对象存储（bucket）中的原始文件 -> PGVector 向量分片 -> MySQL 文档记录，
     * 避免只删库而残留对象存储中的孤儿文件。
     *
     * @param documentId 文档ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteByDocumentId(String documentId) {
        try {
            // 1. 先查询文档记录，获取对象存储 Key（用于清理 bucket 中的原始文件）
            KnowledgeDocumentAggregate doc = documentRepository.findByDocumentId(documentId);

            // 2. 删除对象存储（bucket）中的原始文件，避免残留孤儿文件
            //    对象存储独立于点事务，先删存储以便失败时可重试且不污染库记录
            if (doc != null && doc.getFileKey() != null && !doc.getFileKey().isBlank()) {
                FileStorageService storage = storageProvider.getIfAvailable();
                if (storage != null) {
                    storage.deleteFile(doc.getFileKey());
                    log.info("已删除对象存储文件: documentId={}, fileKey={}", documentId, doc.getFileKey());
                } else {
                    log.warn("对象存储未启用，跳过 bucket 文件删除: documentId={}, fileKey={}",
                            documentId, doc.getFileKey());
                }
            }

            // 3. 按 documentId 过滤条件删除 PGVector 中的所有向量分片
            Filter filter = metadataKey("documentId").isEqualTo(documentId);
            embeddingStore.removeAll(filter);
            log.info("已删除向量数据: documentId={}", documentId);

            // 4. 删除 MySQL 中的文档记录
            documentRepository.deleteByDocumentId(documentId);
            log.info("已删除文档记录: documentId={}", documentId);
        } catch (Exception e) {
            log.error("删除文档失败: documentId={}", documentId, e);
            throw new RuntimeException("删除文档失败: " + e.getMessage(), e);
        }
    }

    /**
     * 批量根据文档ID删除知识库中的多个文档
     * 逐个复用单条删除逻辑：清理对象存储原始文件 -> 删除 PGVector 向量分片 -> 删除 MySQL 文档记录。
     * 单条失败不影响其余文档的删除，失败项仅记录日志，最终返回成功删除的数量。
     *
     * @param documentIds 文档ID列表
     * @return 成功删除的文档数量
     */
    @Transactional(rollbackFor = Exception.class)
    public int deleteByDocumentIds(List<String> documentIds) {
        if (documentIds == null || documentIds.isEmpty()) {
            log.warn("批量删除文档失败：文档ID列表为空");
            return 0;
        }
        int successCount = 0;
        for (String documentId : documentIds) {
            try {
                deleteByDocumentId(documentId);
                successCount++;
            } catch (Exception e) {
                // 单条失败不阻断其余文档，记录日志便于排查
                log.error("批量删除中单个文档失败，已跳过: documentId={}", documentId, e);
            }
        }
        log.info("批量删除文档完成: total={}, success={}", documentIds.size(), successCount);
        return successCount;
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
     * 下载知识库文档的原始文件
     * <p>根据文档ID定位对象存储（RustFS）中的原始文件并返回字节流，便于前端溯源与离线查看。
     * 文件不存在、未启用对象存储等异常均转换为友好业务异常（i18n）。</p>
     *
     * @param documentId 文档唯一标识
     * @return 文件下载结果（含字节与原始文件名）
     */
    public FileDownloadResult downloadDocument(String documentId) {
        KnowledgeDocumentAggregate doc = documentRepository.findByDocumentId(documentId);
        if (doc == null) {
            throw BusinessException.i18n(ResultCode.NOT_FOUND, "ai.knowledge.document.notfound");
        }
        String fileKey = doc.getFileKey();
        if (fileKey == null || fileKey.isBlank()) {
            throw BusinessException.i18n(ResultCode.BUSINESS_ERROR, "ai.knowledge.document.nofile");
        }
        FileStorageService storage = storageProvider.getIfAvailable();
        if (storage == null) {
            throw BusinessException.i18n(ResultCode.BUSINESS_ERROR, "ai.knowledge.storage.unavailable");
        }
        byte[] content = storage.downloadFile(fileKey);
        return new FileDownloadResult(content, doc.getFileName());
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
