package com.silverwing.ai.infrastructure.adapter.llm;

import com.silverwing.ai.domain.port.LlmPort;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.output.Response;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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
        List<ChatMessage> messages = new ArrayList<>();
        if (systemPrompt != null && !systemPrompt.isBlank()) {
            messages.add(SystemMessage.from(systemPrompt));
        }
        messages.add(UserMessage.from(userPrompt));
        Response<AiMessage> response = chatModel.generate(messages);
        return response.content().text();
    }
}
