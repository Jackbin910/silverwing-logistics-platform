package com.silverwing.biz.ai.domain.repository;

import com.silverwing.biz.ai.domain.entity.KnowledgeDocumentAggregate;

/**
 * 知识库文档领域仓储接口
 */
public interface KnowledgeDocumentRepository {

    /**
     * 保存知识库文档记录
     *
     * @param document 文档聚合根
     */
    void insert(KnowledgeDocumentAggregate document);

    /**
     * 根据主键更新文档记录
     *
     * @param document 文档聚合根
     */
    void updateById(KnowledgeDocumentAggregate document);

    /**
     * 清空所有文档记录
     */
    void deleteAll();

    /**
     * 根据文档唯一标识删除文档记录
     *
     * @param documentId 文档唯一标识
     */
    void deleteByDocumentId(String documentId);
}
