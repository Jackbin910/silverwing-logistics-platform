package com.silverwing.ai.infrastructure.adapter.llm;

import com.silverwing.ai.domain.port.LlmPort;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.chat.request.ResponseFormatType;
import org.springframework.stereotype.Component;

/**
 * LLM 端口的 LangChain4j 实现。
 * 收口 ChatModel 的调用细节，是整个基础设施层中唯一直接依赖 LangChain4j 大模型 API 的地方。
 */
@Component
public class LangChain4jLlmAdapter implements LlmPort {

    /**
     * JSON 模式约束：不带 schema，Ollama 侧映射为 format=json，兼容性最好
     */
    private static final ResponseFormat JSON_RESPONSE_FORMAT = ResponseFormat.builder()
            .type(ResponseFormatType.JSON)
            .build();

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
        return chatModel.chat(buildFullPrompt(systemPrompt, userPrompt));
    }

    @Override
    public String completeJson(String systemPrompt, String userPrompt, Double temperature) {
        // 通过 ChatRequest 按调用覆盖温度与响应格式；提示词文本与既有写法逐字一致
        ChatRequest.Builder builder = ChatRequest.builder()
                .messages(UserMessage.from(buildFullPrompt(systemPrompt, userPrompt)))
                .responseFormat(JSON_RESPONSE_FORMAT);
        if (temperature != null) {
            builder.temperature(temperature);
        }
        return chatModel.chat(builder.build()).aiMessage().text();
    }

    /**
     * 拼接系统提示词与用户提示词，保持与既有调用一致
     *
     * @param systemPrompt 系统提示词
     * @param userPrompt   用户提示词
     * @return 拼接后的完整提示词
     */
    private String buildFullPrompt(String systemPrompt, String userPrompt) {
        return (systemPrompt == null || systemPrompt.isBlank())
                ? userPrompt
                : systemPrompt + "\n\n" + userPrompt;
    }
}
