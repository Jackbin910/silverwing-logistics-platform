package com.silverwing.biz.ai.domain.repository;

import com.silverwing.biz.ai.domain.entity.KnowledgeDocumentAggregate;
import com.silverwing.common.domain.PageResult;

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

    /**
     * 分页查询知识库文档列表
     *
     * @param current 当前页（从 1 开始）
     * @param size    每页条数
     * @param keyword 标题关键词（可选，模糊匹配）
     * @param status  文档状态（可选：0待处理/1已导入/2失败）
     * @return 分页结果（领域聚合根）
     */
    PageResult<KnowledgeDocumentAggregate> pageDocuments(long current, long size, String keyword, Integer status);

    /**
     * 根据文档唯一标识查询单条文档
     *
     * @param documentId 文档唯一标识
     * @return 文档聚合根，不存在时返回 null
     */
    KnowledgeDocumentAggregate findByDocumentId(String documentId);
}
