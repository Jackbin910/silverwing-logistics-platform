package com.silverwing.biz.ai.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.silverwing.biz.ai.domain.entity.KnowledgeDocumentAggregate;
import com.silverwing.biz.ai.domain.repository.KnowledgeDocumentRepository;
import com.silverwing.biz.ai.infrastructure.convertor.KnowledgeDocumentInfraConvertor;
import com.silverwing.biz.ai.infrastructure.dao.po.KnowledgeDocumentPO;
import com.silverwing.biz.ai.infrastructure.mapper.KnowledgeDocumentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 知识库文档仓储实现（基于 MyBatis-Plus Mapper）
 * <p>经 KnowledgeDocumentInfraConvertor 在 PO 与领域聚合根之间转换，落实防腐。</p>
 */
@Repository
@RequiredArgsConstructor
public class KnowledgeDocumentRepositoryImpl implements KnowledgeDocumentRepository {

    private final KnowledgeDocumentMapper knowledgeDocumentMapper;

    @Override
    public void insert(KnowledgeDocumentAggregate document) {
        knowledgeDocumentMapper.insert(
                KnowledgeDocumentInfraConvertor.INSTANCE.toPo(document));
    }

    @Override
    public void updateById(KnowledgeDocumentAggregate document) {
        knowledgeDocumentMapper.updateById(
                KnowledgeDocumentInfraConvertor.INSTANCE.toPo(document));
    }

    @Override
    public void deleteAll() {
        knowledgeDocumentMapper.delete(null);
    }

    @Override
    public void deleteByDocumentId(String documentId) {
        LambdaQueryWrapper<KnowledgeDocumentPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeDocumentPO::getDocumentId, documentId);
        knowledgeDocumentMapper.delete(wrapper);
    }
}
