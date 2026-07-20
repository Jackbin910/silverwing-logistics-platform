package com.silverwing.ai.domain.service;

import com.silverwing.ai.domain.model.SummaryResult;
import com.silverwing.ai.domain.port.LlmPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 文本摘要服务实现
 */
@Slf4j
@Service
public class TextSummaryService {

    private static final String SYSTEM_PROMPT = """
        你是物流智能平台的文本摘要引擎。请对以下文本生成简洁的摘要。

        要求：
        1. 摘要长度不超过原文的 30%
        2. 保留关键信息（时间、地点、设备、故障原因、处理结果）
        3. 使用简洁的中文
        4. 直接返回摘要内容，不要有多余的格式
        """;

    private final LlmPort llmPort;

    /**
     * 构造函数
     *
     * @param llmPort LLM 调用端口
     */
    public TextSummaryService(LlmPort llmPort) {
        this.llmPort = llmPort;
    }

    /**
     * 对文本生成摘要
     *
     * @param text 待摘要的文本
     * @return 摘要结果
     */
    public SummaryResult summarize(String text) {
        try {
            String summary = llmPort.complete(SYSTEM_PROMPT, text);
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
