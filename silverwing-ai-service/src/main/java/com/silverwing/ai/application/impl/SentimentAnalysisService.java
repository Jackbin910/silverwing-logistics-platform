package com.silverwing.ai.application.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.silverwing.ai.application.dto.SentimentResult;
import com.silverwing.ai.application.ai.SentimentAnalyzer;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 情感分析服务实现
 */
@Slf4j
@Service
public class SentimentAnalysisService {

    private final SentimentAnalyzer sentimentAnalyzer;

    /**
     * 构造函数
     *
     * @param chatModel LangChain4j 聊天模型
     */
    public SentimentAnalysisService(ChatModel chatModel) {
        this.sentimentAnalyzer = AiServices.create(SentimentAnalyzer.class, chatModel);
    }

    /**
     * 分析文本的情感倾向
     *
     * @param text 待分析的文本
     * @return 情感分析结果
     */
    public SentimentResult analyze(String text) {
        try {
            String jsonResult = sentimentAnalyzer.analyze(text);
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
