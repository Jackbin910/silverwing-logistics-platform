package com.silverwing.ai.application.convertor;

import com.silverwing.ai.application.dto.DbTableSchemaDTO;
import com.silverwing.ai.application.dto.KnowledgeDocumentDTO;
import com.silverwing.biz.ai.domain.entity.DbTableSchemaAggregate;
import com.silverwing.biz.ai.domain.entity.KnowledgeDocumentAggregate;
import org.mapstruct.Mapper;

/**
 * AI 应用层转换器
 * <p>
 * 负责 biz-ai 领域聚合根（DbTableSchemaAggregate / KnowledgeDocumentAggregate）与应用层 DTO 的映射，
 * 隔离领域模型与对外传输模型，对齐 auth 模块的 AuthConvertor（MapStruct + Spring 注入）。
 * </p>
 */
@Mapper(componentModel = "spring")
public interface AiConvertor {

    /**
     * 数据库表结构聚合根 -> 响应DTO
     */
    DbTableSchemaDTO toDbTableSchemaDto(DbTableSchemaAggregate aggregate);

    /**
     * 知识库文档聚合根 -> 响应DTO
     */
    KnowledgeDocumentDTO toKnowledgeDocumentDto(KnowledgeDocumentAggregate aggregate);
}
