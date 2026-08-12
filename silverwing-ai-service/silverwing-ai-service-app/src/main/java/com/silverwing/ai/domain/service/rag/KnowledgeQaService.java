package com.silverwing.ai.domain.service.rag;

import cn.hutool.core.util.StrUtil;
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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
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
            // 1. 检索相关知识片段（含 Query 改写多路召回）
            List<EmbeddingMatch<TextSegment>> relevantMatches = retrieve(question);
            if (relevantMatches.isEmpty()) {
                log.info("未检索到与问题相关的知识内容: {}", question);
                return "抱歉，知识库中暂未找到与您问题相关的内容。\n\n您可以尝试：\n"
                        + "- 换一种方式描述您的问题\n"
                        + "- 确认知识库中是否已导入相关文档";
            }

            log.info("检索到 {} 条相关知识片段，开始生成回答", relevantMatches.size());

            // 2. 构建上下文并生成回答
            String context = buildContext(relevantMatches);
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
            // 1. 检索相关知识片段（含 Query 改写多路召回）
            List<EmbeddingMatch<TextSegment>> relevantMatches = retrieve(question);
            if (relevantMatches.isEmpty()) {
                // 2. 无匹配时直接返回兜底文本
                return Flux.just("抱歉，知识库中暂未找到与您问题相关的内容。\n\n您可以尝试：\n- 换一种方式描述您的问题\n- 确认知识库中是否已导入相关文档");
            }

            // 3. 构建上下文与 ChatRequest
            String context = buildContext(relevantMatches);
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
     * 检索相关知识片段（含 Query 改写多路召回）
     * <p>参考 WeKnora 的检索增强思想：先用 LLM 将用户口语化问题改写为若干标准检索问句，
     * 对原问题与改写问句分别向量检索，合并去重后返回 topN 候选片段，缓解口语/书面语语义鸿沟。</p>
     *
     * @param question 用户原始问题
     * @return 去重合并后的相关知识片段（按相关性排序，最多 DEFAULT_MAX_RESULTS 条）
     */
    private List<EmbeddingMatch<TextSegment>> retrieve(String question) {
        List<String> queries = rewriteQuery(question);
        log.info("Query 改写完成，检索问句数={}：{}", queries.size(), queries);

        Set<String> seen = new LinkedHashSet<>();
        List<EmbeddingMatch<TextSegment>> merged = new ArrayList<>();
        for (String q : queries) {
            Embedding qEmbedding = embeddingModel.embed(q).content();
            EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                    .queryEmbedding(qEmbedding)
                    .maxResults(DEFAULT_MAX_RESULTS)
                    .minScore(DEFAULT_MIN_SCORE)
                    .build();
            for (EmbeddingMatch<TextSegment> match : embeddingStore.search(request).matches()) {
                // 以分片文本为去重键，避免多路召回重复片段
                if (seen.add(match.embedded().text())) {
                    merged.add(match);
                }
            }
        }
        // 超出上限时截断（保留先召回的高相关片段）
        return merged.size() > DEFAULT_MAX_RESULTS
                ? merged.subList(0, DEFAULT_MAX_RESULTS)
                : merged;
    }

    /**
     * 将用户问题改写为若干标准检索问句
     * <p>物流场景口语与文档书面语差异大（如「货到了吗」vs「入库完成状态」），
     * 通过 LLM 生成 1~3 个规范化检索问句提升召回率。改写失败则回退到原问题。</p>
     *
     * @param question 用户原始问题
     * @return 检索问句列表（至少包含原问题）
     */
    private List<String> rewriteQuery(String question) {
        List<String> result = new ArrayList<>();
        result.add(question);
        try {
            String prompt = """
                    你是物流仓储知识库的检索优化助手。请将用户的问题改写为 1 到 3 个\
                    适合在知识库中向量检索的标准问句，保留核心实体与专业术语，去掉口语化表达。
                    每行一个问句，不要编号、不要解释。
                    """.replace("\\", "") + "\n用户问题：" + question;
            String rewritten = chatModel.chat(prompt).trim();
            for (String line : rewritten.split("\\n")) {
                String q = line.trim();
                // 去除可能的项目符号
                q = StrUtil.removePrefix(q, "- ").trim();
                q = StrUtil.removePrefix(q, "• ").trim();
                if (!q.isEmpty() && !q.equals(question)) {
                    result.add(q);
                }
            }
        } catch (Exception e) {
            // 改写失败不影响主流程，直接使用原问题检索
            log.warn("Query 改写失败，回退原问题检索: {}", question, e);
        }
        return result;
    }

    /**
     * 将检索到的知识片段拼装为上下文文本
     * <p>每个片段标注文档标题与标题面包屑（headingPath），便于 LLM 理解出处层级。</p>
     *
     * @param matches 去重合并后的知识片段
     * @return 拼接后的上下文文本
     */
    private String buildContext(List<EmbeddingMatch<TextSegment>> matches) {
        return matches.stream()
                .map(match -> {
                    TextSegment segment = match.embedded();
                    String title = segment.metadata().getString("title");
                    String headingPath = segment.metadata().getString("headingPath");
                    StringBuilder label = new StringBuilder("【");
                    label.append(title != null ? title : "未命名文档");
                    if (headingPath != null && !headingPath.isEmpty()) {
                        label.append(" / ").append(headingPath);
                    }
                    label.append("】");
                    return label + "\n" + segment.text();
                })
                .collect(Collectors.joining("\n\n---\n\n"));
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
