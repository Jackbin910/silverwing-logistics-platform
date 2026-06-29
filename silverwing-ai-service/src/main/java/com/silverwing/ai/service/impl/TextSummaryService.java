package com.silverwing.ai.service.impl;

import com.silverwing.ai.domain.dto.SummaryResult;
import com.silverwing.ai.service.ai.TextSummarizer;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 文本摘要服务实现
 */
@Slf4j
@Service
public class TextSummaryService {

    private final TextSummarizer textSummarizer;

    /**
     * 构造函数
     *
     * @param chatModel LangChain4j 聊天模型
     */
    public TextSummaryService(ChatModel chatModel) {
        this.textSummarizer = AiServices.create(TextSummarizer.class, chatModel);
    }

    /**
     * 对文本生成摘要
     *
     * @param text 待摘要的文本
     * @return 摘要结果
     */
    public SummaryResult summarize(String text) {
        try {
            String summary = textSummarizer.summarize(text);
            log.info("文本摘要完成，原文长度: {}，摘要长度: {}", text.length(), summary.length());

            return SummaryResult.builder()
                    .summary(summary)
                    .originalLength(text.length())
                    .summaryLength(summary.length())
                    .build();
        } catch (Exception e) {
            log.error("文本摘要失败", e);
            return SummaryResult.builder()
                    .summary("摘要生成失败")
                    .originalLength(text.length())
                    .summaryLength(0)
                    .build();
        }
    }
}
