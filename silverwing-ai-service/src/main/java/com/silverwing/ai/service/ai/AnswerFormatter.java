package com.silverwing.ai.service.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * 回答格式化 AI 接口
 * 将业务查询的结构化数据转化为自然语言回答
 */
public interface AnswerFormatter {

    @SystemMessage("""
        你是物流智能平台的智能助手。请根据业务查询结果，用简洁友好的中文回答用户的问题。

        要求：
        1. 直接回答问题，不要重复用户的问题
        2. 使用专业的物流/设备术语
        3. 如果查询结果为空，告知用户未找到相关信息
        4. 适当组织信息，使用分点或分行让回答更清晰
        5. 如果有数值数据，带上合适的单位
        """)
    String format(@UserMessage String userQuestion, @UserMessage String queryResultJson);
}
