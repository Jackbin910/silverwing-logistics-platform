package com.silverwing.ai.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.silverwing.ai.domain.entity.KnowledgeDocument;
import org.apache.ibatis.annotations.Mapper;

/**
 * 知识库文档 Mapper
 */
@Mapper
public interface KnowledgeDocumentMapper extends BaseMapper<KnowledgeDocument> {
}
