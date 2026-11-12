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

    /**
     * 发起一次 JSON 模式的对话补全。
     * <p>实现层要求模型输出合法 JSON 对象，配合指定温度用于意图识别、实体抽取等结构化解析任务，
     * 减少因模型附带解释文字或 markdown 代码块导致的解析失败与重试。</p>
     *
     * @param systemPrompt 系统提示词，可为空（不传则仅使用用户提示词）
     * @param userPrompt   用户提示词
     * @param temperature  本次调用的温度，null 表示沿用模型全局配置
     * @return 模型返回的 JSON 文本
     */
    String completeJson(String systemPrompt, String userPrompt, Double temperature);
}
