package com.silverwing.ai.application.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * RAG 知识库问答 AI 接口
 * 通过 LangChain4j AiServices + ContentRetriever 实现检索增强生成
 */
public interface KnowledgeAssistant {

    @SystemMessage("""
        你是物流仓储智能助手，专门根据知识库中的参考资料回答用户的问题。

        回答规则：
        1. 仅根据提供的参考资料回答，不要编造或猜测信息
        2. 如果参考资料不足以回答问题，请如实说明"知识库中暂未找到相关内容"
        3. 回答要简洁专业，使用物流/仓储领域的标准术语
        4. 适当组织信息，使用分点或分行让回答更清晰
        5. 如果参考资料中有具体数值或标准，请引用出来
        """)
    String answer(@UserMessage @V("question") String question);
}
