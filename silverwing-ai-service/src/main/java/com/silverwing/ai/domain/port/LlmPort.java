package com.silverwing.ai.domain.port;

/**
 * LLM 调用领域端口（出站端口）。
 * 屏蔽底层大模型框架（LangChain4j / Ollama 等）差异，
 * 领域服务仅依赖此端口完成一次对话补全，不感知具体实现。
 */
public interface LlmPort {

    /**
     * 发起一次对话补全。
     *
     * @param systemPrompt 系统提示词，可为空（不传则仅使用用户提示词）
     * @param userPrompt   用户提示词
     * @return 模型返回的文本内容
     */
    String complete(String systemPrompt, String userPrompt);
}
