package com.silverwing.ai.domain.service.rag;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 知识库问答服务（RAG）
 * 基于向量相似度检索知识库内容，结合 LLM 生成回答
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeQaService {

    private final EmbeddingModel embeddingModel;

    private final EmbeddingStore<TextSegment> embeddingStore;

    private final ChatModel chatModel;

    private final StreamingChatModel streamingChatModel;


    /**
     * 默认检索最大结果数
     */
    private static final int DEFAULT_MAX_RESULTS = 8;
    /**
     * 默认最低相似度分数
     */
    private static final double DEFAULT_MIN_SCORE = 0.5;

    /**
     * 基于知识库回答用户问题
     *
     * @param question 用户问题
     * @return LLM 生成的回答
     */
    public String answer(String question) {
        try {
            // 1. 将问题向量化
            Embedding questionEmbedding = embeddingModel.embed(question).content();

            // 2. 从向量库检索最相关的文档片段
            EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                    .queryEmbedding(questionEmbedding)
                    .maxResults(DEFAULT_MAX_RESULTS)
                    .minScore(DEFAULT_MIN_SCORE)
                    .build();

            List<EmbeddingMatch<TextSegment>> relevantMatches = embeddingStore.search(searchRequest)
                    .matches();

            // 3. 构建上下文
            String context = relevantMatches.stream()
                    .map(match -> {
                        TextSegment segment = match.embedded();
                        String source = segment.metadata().getString("title");
                        return "【" + (source != null ? source : "未命名文档") + "】\n" + segment.text();
                    })
                    .collect(Collectors.joining("\n\n---\n\n"));

            if (context.isBlank()) {
                log.info("未检索到与问题相关的知识内容: {}", question);
                return "抱歉，知识库中暂未找到与您问题相关的内容。\n\n您可以尝试：\n"
                        + "- 换一种方式描述您的问题\n"
                        + "- 确认知识库中是否已导入相关文档";
            }

            log.info("检索到 {} 条相关知识片段，开始生成回答", relevantMatches.size());

            // 4. 使用 Chat 模型基于上下文生成回答
            String prompt = buildRagPrompt(question, context);
            return chatModel.chat(prompt);

        } catch (Exception e) {
            log.error("知识库问答失败: {}", question, e);
            return "抱歉，在处理您的问题时遇到了技术问题，请稍后重试。";
        }
    }

    /**
     * 基于知识库流式回答用户问题
     * 向量检索（同步）完成后，通过回调逐 token 推送 LLM 生成的回答
     */
    public Flux<String> answerStream(String question) {
        return Flux.defer(() -> {
            // 1. 向量检索（同步阻塞）
            Embedding questionEmbedding = embeddingModel.embed(question).content();
            EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                    .queryEmbedding(questionEmbedding)
                    .maxResults(DEFAULT_MAX_RESULTS)
                    .minScore(DEFAULT_MIN_SCORE)
                    .build();

            List<EmbeddingMatch<TextSegment>> relevantMatches = embeddingStore.search(searchRequest).matches();

            // 2. 构建上下文
            String context = relevantMatches.stream()
                    .map(match -> {
                        TextSegment segment = match.embedded();
                        String source = segment.metadata().getString("title");
                        return "【" + (source != null ? source : "未命名文档") + "】\n" + segment.text();
                    })
                    .collect(Collectors.joining("\n\n---\n\n"));

            if (context.isBlank()) {
                // 3. 无匹配时直接返回兜底文本
                return Flux.just("抱歉，知识库中暂未找到与您问题相关的内容。\n\n您可以尝试：\n- 换一种方式描述您的问题\n- 确认知识库中是否已导入相关文档");
            }

            // 4. 构建 ChatRequest
            String prompt = buildRagPrompt(question, context);
            ChatRequest chatRequest = ChatRequest.builder()
                    .messages(UserMessage.from(prompt))
                    .build();

            // 5. 使用 StreamingChatResponseHandler 桥接为 Flux
            return Flux.create(sink -> {
                streamingChatModel.chat(chatRequest, new StreamingChatResponseHandler() {
                    @Override
                    public void onPartialResponse(String partialResponse) {
                        sink.next(partialResponse);
                    }

                    @Override
                    public void onCompleteResponse(ChatResponse completeResponse) {
                        sink.complete();
                    }

                    @Override
                    public void onError(Throwable error) {
                        sink.error(error);
                    }
                });
            });
        });
    }



    /**
     * 构建 RAG 问答 Prompt
     *
     * @param question 用户问题
     * @param context  检索到的知识库上下文
     * @return 完整的 Prompt
     */
    private String buildRagPrompt(String question, String context) {
        return """
                你是物流仓储智能助手。请根据以下参考资料回答用户的问题。

                回答要求：
                1. 仅根据参考资料回答，不要编造或推测信息
                2. 如果参考资料不足以完全回答问题，请如实说明并提供已有的相关信息
                3. 回答要简洁专业，使用合适的物流/设备术语
                4. 如果参考资料中有具体数据（温度、容量、速度等），请准确引用
                5. 使用分点或分行让回答更清晰易读

                【参考资料】
                %s

                【用户问题】
                %s
                """.formatted(context, question);
    }
}
