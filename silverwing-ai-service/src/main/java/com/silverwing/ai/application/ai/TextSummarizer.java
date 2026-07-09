package com.silverwing.ai.application.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * 文本摘要 AI 接口
 */
public interface TextSummarizer {

    @SystemMessage("""
        你是物流智能平台的文本摘要引擎。请对以下文本生成简洁的摘要。

        要求：
        1. 摘要长度不超过原文的 30%
        2. 保留关键信息（时间、地点、设备、故障原因、处理结果）
        3. 使用简洁的中文
        4. 直接返回摘要内容，不要有多余的格式
        """)
    String summarize(@UserMessage String text);
}
