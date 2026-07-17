package com.silverwing.ai.infrastructure.adapter.llm;

import com.silverwing.ai.domain.port.LlmPort;
import dev.langchain4j.model.chat.ChatModel;
import org.springframework.stereotype.Component;

/**
 * LLM 端口的 LangChain4j 实现。
 * 收口 ChatModel 的调用细节，是整个基础设施层中唯一直接依赖 LangChain4j 大模型 API 的地方。
 */
@Component
public class LangChain4jLlmAdapter implements LlmPort {

    private final ChatModel chatModel;

    /**
     * 构造函数，注入由 langchain4j-ollama-spring-boot-starter 自动配置的 ChatModel。
     *
     * @param chatModel LangChain4j 聊天模型
     */
    public LangChain4jLlmAdapter(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public String complete(String systemPrompt, String userPrompt) {
        // 与项目内已验证的用法一致：chat(String) 直接返回文本。
        // 系统提示词与用户提示词拼接为单条请求，保持对底层框架版本的兼容。
        String fullPrompt = (systemPrompt == null || systemPrompt.isBlank())
                ? userPrompt
                : systemPrompt + "\n\n" + userPrompt;
        return chatModel.chat(fullPrompt);
    }
}
