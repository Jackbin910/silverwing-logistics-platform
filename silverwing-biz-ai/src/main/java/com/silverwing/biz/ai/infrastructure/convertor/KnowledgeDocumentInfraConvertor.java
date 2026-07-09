package com.silverwing.biz.ai.infrastructure.convertor;

import com.silverwing.biz.ai.domain.entity.KnowledgeDocumentAggregate;
import com.silverwing.biz.ai.infrastructure.dao.po.KnowledgeDocumentPO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 知识库文档基础设施转换器（防腐层）
 * <p>负责 PO（KnowledgeDocumentPO）与领域实体（KnowledgeDocumentAggregate）互转，使用 MapStruct 编译期生成。</p>
 */
@Mapper
public interface KnowledgeDocumentInfraConvertor {

    /** 静态单例，供仓储实现直接调用 */
    KnowledgeDocumentInfraConvertor INSTANCE = Mappers.getMapper(KnowledgeDocumentInfraConvertor.class);

    /** 领域实体 -> 持久化对象 */
    KnowledgeDocumentPO toPo(KnowledgeDocumentAggregate aggregate);

    /** 持久化对象 -> 领域实体 */
    KnowledgeDocumentAggregate toDomain(KnowledgeDocumentPO po);
}
