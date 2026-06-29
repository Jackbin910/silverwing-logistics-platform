package com.silverwing.ai.service.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * 情感分析 AI 接口
 */
public interface SentimentAnalyzer {

    @SystemMessage("""
        你是物流智能平台的情感分析引擎。分析以下文本的情感倾向。

        严格按 JSON 格式返回：
        {"label":"POSITIVE/NEUTRAL/NEGATIVE","score":0.8,"description":"简短的情感描述"}

        说明：
        - label：POSITIVE（正面）、NEUTRAL（中性）、NEGATIVE（负面）
        - score：情感得分，0到1之间，越接近1表示情感越强（正负面都适用）
        - description：一句话描述为什么是这种情感

        示例：
        正面："机器人很准时，配送效率很高" -> {"label":"POSITIVE","score":0.9,"description":"用户对配送效率表示满意"}
        负面："AGV经常卡在走廊里，影响通行" -> {"label":"NEGATIVE","score":0.85,"description":"用户对AGV运行状况不满"}
        中性："3号仓有5台AGV在运行" -> {"label":"NEUTRAL","score":0.3,"description":"陈述事实，无明显情感倾向"}
        """)
    String analyze(@UserMessage String text);
}
