package com.silverwing.biz.ai.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.silverwing.biz.ai.domain.entity.KnowledgeDocument;
import com.silverwing.biz.ai.domain.repository.KnowledgeDocumentRepository;
import com.silverwing.biz.ai.infrastructure.mapper.KnowledgeDocumentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 知识库文档仓储实现（基于 MyBatis-Plus Mapper）
 */
@Repository
@RequiredArgsConstructor
public class KnowledgeDocumentRepositoryImpl implements KnowledgeDocumentRepository {

    private final KnowledgeDocumentMapper knowledgeDocumentMapper;

    @Override
    public void insert(KnowledgeDocument document) {
        knowledgeDocumentMapper.insert(document);
    }

    @Override
    public void updateById(KnowledgeDocument document) {
        knowledgeDocumentMapper.updateById(document);
    }

    @Override
    public void deleteAll() {
        knowledgeDocumentMapper.delete(null);
    }

    @Override
    public void delete(LambdaQueryWrapper<KnowledgeDocument> wrapper) {
        knowledgeDocumentMapper.delete(wrapper);
    }
}
