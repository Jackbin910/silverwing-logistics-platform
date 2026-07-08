package com.silverwing.biz.ai.domain.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.silverwing.biz.ai.domain.entity.KnowledgeDocument;

/**
 * 知识库文档领域仓储接口
 */
public interface KnowledgeDocumentRepository {

    /**
     * 保存知识库文档记录
     *
     * @param document 文档实体
     */
    void insert(KnowledgeDocument document);

    /**
     * 根据主键更新文档记录
     *
     * @param document 文档实体
     */
    void updateById(KnowledgeDocument document);

    /**
     * 清空所有文档记录
     */
    void deleteAll();

    /**
     * 根据条件删除文档记录
     *
     * @param wrapper 查询条件
     */
    void delete(LambdaQueryWrapper<KnowledgeDocument> wrapper);
}
