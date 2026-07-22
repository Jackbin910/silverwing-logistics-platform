package com.silverwing.biz.ai.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.silverwing.biz.ai.domain.entity.KnowledgeDocumentAggregate;
import com.silverwing.biz.ai.domain.repository.KnowledgeDocumentRepository;
import com.silverwing.biz.ai.infrastructure.convertor.KnowledgeDocumentInfraConvertor;
import com.silverwing.biz.ai.infrastructure.dao.po.KnowledgeDocumentPO;
import com.silverwing.biz.ai.infrastructure.mapper.KnowledgeDocumentMapper;
import com.silverwing.common.domain.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

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

    @Override
    public PageResult<KnowledgeDocumentAggregate> pageDocuments(long current, long size, String keyword, Integer status) {
        // 构造分页对象与查询条件（关键词模糊匹配标题，状态等值，按创建时间倒序）
        Page<KnowledgeDocumentPO> page = new Page<>(current, size);
        LambdaQueryWrapper<KnowledgeDocumentPO> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(KnowledgeDocumentPO::getTitle, keyword);
        }
        if (status != null) {
            wrapper.eq(KnowledgeDocumentPO::getStatus, status);
        }
        wrapper.orderByDesc(KnowledgeDocumentPO::getCreateTime);

        IPage<KnowledgeDocumentPO> iPage = knowledgeDocumentMapper.selectPage(page, wrapper);
        List<KnowledgeDocumentAggregate> records = iPage.getRecords().stream()
                .map(KnowledgeDocumentInfraConvertor.INSTANCE::toDomain)
                .collect(Collectors.toList());
        return new PageResult<>(iPage.getCurrent(), iPage.getSize(), iPage.getTotal(), records);
    }

    @Override
    public KnowledgeDocumentAggregate findByDocumentId(String documentId) {
        LambdaQueryWrapper<KnowledgeDocumentPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeDocumentPO::getDocumentId, documentId);
        KnowledgeDocumentPO po = knowledgeDocumentMapper.selectOne(wrapper);
        return po == null ? null : KnowledgeDocumentInfraConvertor.INSTANCE.toDomain(po);
    }
}
