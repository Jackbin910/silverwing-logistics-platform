package com.silverwing.ai.domain.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.silverwing.ai.domain.model.SentimentResult;
import com.silverwing.ai.domain.port.LlmPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 情感分析服务实现
 */
@Slf4j
@Service
public class SentimentAnalysisService {

    private static final String SYSTEM_PROMPT = """
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
        """;

    private final LlmPort llmPort;

    /**
     * 构造函数
     *
     * @param llmPort LLM 调用端口
     */
    public SentimentAnalysisService(LlmPort llmPort) {
        this.llmPort = llmPort;
    }

    /**
     * 分析文本的情感倾向
     *
     * @param text 待分析的文本
     * @return 情感分析结果
     */
    public SentimentResult analyze(String text) {
        try {
            String jsonResult = llmPort.complete(SYSTEM_PROMPT, text);
            log.info("情感分析结果: {}", jsonResult);

            // 解析 JSON 结果
            String cleaned = jsonResult.trim();
            if (cleaned.startsWith("```")) {
                cleaned = cleaned.replaceAll("^```(?:json)?\\s*", "").replaceAll("\\s*```$", "");
            }

            JSONObject obj = JSON.parseObject(cleaned);
            return SentimentResult.builder()
                    .label(obj.getString("label"))
                    .score(obj.getDouble("score"))
                    .description(obj.getString("description"))
                    .build();
        } catch (Exception e) {
            log.error("情感分析失败", e);
            return SentimentResult.builder()
                    .label("NEUTRAL")
                    .score(0.0)
                    .description("分析失败")
                    .build();
        }
    }
}
