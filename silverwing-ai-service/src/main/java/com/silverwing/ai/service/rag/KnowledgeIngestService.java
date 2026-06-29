package com.silverwing.ai.service.rag;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.silverwing.ai.domain.dto.KnowledgeIngestRequest;
import com.silverwing.ai.domain.dto.KnowledgeIngestResult;
import com.silverwing.ai.domain.entity.KnowledgeDocument;
import com.silverwing.ai.domain.mapper.KnowledgeDocumentMapper;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 知识库导入服务
 * 负责将文档内容切分、向量化并存入 PGVector 向量数据库
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

    private final KnowledgeDocumentMapper documentMapper;

    /**
     * 导入知识库文档（带元信息记录）
     *
     * @param request 知识导入请求
     * @return 导入结果
     */
    @Transactional(rollbackFor = Exception.class)
    public KnowledgeIngestResult ingest(KnowledgeIngestRequest request) {
        // 1. 生成文档ID
        String documentId = IdUtil.fastSimpleUUID();

        // 2. 初始化文档记录（待处理状态）
        KnowledgeDocument doc = new KnowledgeDocument();
        doc.setDocumentId(documentId);
        doc.setTitle(request.getTitle());
        doc.setCategory(request.getCategory());
        doc.setSourceType(request.getSourceType());
        doc.setWarehouseId(request.getWarehouseId());
        doc.setDeviceType(request.getDeviceType());
        doc.setWordCount(request.getContent().length());
        doc.setStatus(0); // 待处理
        documentMapper.insert(doc);

        try {
            // 3. 执行向量导入
            int chunkCount = ingest(
                    request.getTitle(),
                    request.getContent(),
                    request.getCategory(),
                    request.getSourceType(),
                    request.getWarehouseId(),
                    request.getDeviceType()
            );

            // 4. 更新文档状态为已导入
            doc.setChunkCount(chunkCount);
            doc.setStatus(1); // 已导入
            documentMapper.updateById(doc);

            return KnowledgeIngestResult.builder()
                    .documentId(documentId)
                    .chunkCount(chunkCount)
                    .wordCount(request.getContent().length())
                    .build();

        } catch (Exception e) {
            // 更新文档状态为导入失败
            doc.setStatus(2);
            doc.setErrorMsg(e.getMessage());
            documentMapper.updateById(doc);
            throw new RuntimeException("知识库导入失败: " + e.getMessage(), e);
        }
    }

    /**
     * 导入纯文本文档到知识库
     * 自动按段落切分、向量化、存入向量数据库
     *
     * @param title      文档标题
     * @param content    文档内容
     * @param category   文档分类（如：设备手册、FAQ）
     * @param sourceType 来源类型（如：manual、web）
     * @return 导入的分片数量
     */
    public int ingest(String title, String content, String category, String sourceType) {
        return ingest(title, content, category, sourceType, null, null);
    }

    /**
     * 导入纯文本文档到知识库（带仓库和设备类型过滤）
     *
     * @param title       文档标题
     * @param content     文档内容
     * @param category    文档分类
     * @param sourceType  来源类型
     * @param warehouseId 仓库 ID（可选）
     * @param deviceType  设备类型（可选）
     * @return 导入的分片数量
     */
    public int ingest(String title, String content, String category, String sourceType,
                      String warehouseId, String deviceType) {
        try {
            // 1. 构建文档元信息
            Metadata metadata = Metadata.from("title", title);
            putIfPresent(metadata, "category", category);
            putIfPresent(metadata, "sourceType", sourceType);
            putIfPresent(metadata, "warehouseId", warehouseId);
            putIfPresent(metadata, "deviceType", deviceType);

            // 2. 按段落切分文本
            List<TextSegment> chunks = splitIntoChunks(content, metadata);
            log.info("文档切分完成: title={}, 总段落数={}, 切分后分片数={}", title, 0, chunks.size());

            // 3. 批量向量化并存入向量数据库
            List<Embedding> embeddings = embeddingModel.embedAll(chunks).content();

            embeddingStore.addAll(embeddings, chunks);

            log.info("知识库导入成功: title={}, category={}, chunkCount={}", title, category, chunks.size());
            return chunks.size();

        } catch (Exception e) {
            log.error("知识库导入失败: title={}", title, e);
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
                            && currentChunk.length() > 0) {
                        chunks.add(createChunk(currentChunk.toString(), metadata, chunkIndex++));
                        currentChunk = new StringBuilder();
                    }
                    if (currentChunk.length() > 0) {
                        currentChunk.append("\n");
                    }
                    currentChunk.append(lineTrimmed);
                }
            } else {
                if (currentChunk.length() > 0) {
                    currentChunk.append("\n\n");
                }
                currentChunk.append(trimmed);
            }
        }

        // 保存最后一个块
        if (currentChunk.length() > 0) {
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
            documentMapper.delete(null);
            log.info("知识库已清空");
        } catch (Exception e) {
            log.error("清空知识库失败", e);
            throw new RuntimeException("清空知识库失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据文档ID删除知识库中的分片
     * 注意：PGVector目前不支持按metadata删除，这里标记为逻辑删除
     *
     * @param documentId 文档ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteByDocumentId(String documentId) {
        try {
            // 逻辑删除MySQL中的文档记录
            LambdaQueryWrapper<KnowledgeDocument> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(KnowledgeDocument::getDocumentId, documentId);
            documentMapper.delete(wrapper);
            // TODO: 向量库的精确删除需要根据documentId过滤，当前版本暂不支持
            log.info("已删除文档记录: documentId={}", documentId);
        } catch (Exception e) {
            log.error("删除文档失败: documentId={}", documentId, e);
            throw new RuntimeException("删除文档失败: " + e.getMessage(), e);
        }
    }

    /**
     * 向元信息中添加非空字段
     *
     * @param metadata 元信息
     * @param key      键
     * @param value    值
     */
    private void putIfPresent(Metadata metadata, String key, String value) {
        if (value != null && !value.isBlank()) {
            metadata.put(key, value);
        }
    }
}
