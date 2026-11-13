package com.silverwing.ai.application.impl;

import com.alibaba.fastjson2.JSON;
import com.silverwing.ai.application.dto.*;
import com.silverwing.ai.common.util.StreamingCancellation;
import com.silverwing.biz.ai.domain.enums.IntentEnum;
import com.silverwing.ai.domain.service.IntentRecognitionService;
import com.silverwing.ai.domain.service.rag.DatabaseRagService;
import com.silverwing.ai.domain.port.LlmPort;
import com.silverwing.ai.domain.service.rag.KnowledgeQaService;
import com.silverwing.common.exception.BusinessException;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.PartialResponse;
import dev.langchain4j.model.chat.response.PartialResponseContext;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.chat.response.StreamingHandle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import com.silverwing.ai.domain.model.NlpParseResult;
import com.silverwing.ai.domain.model.BizQueryResult;
import com.silverwing.ai.domain.model.ConversationRecord;
import com.silverwing.ai.domain.repository.ConversationRepository;
import com.silverwing.ai.domain.repository.ConversationHistoryRepository;
import cn.dev33.satoken.stp.StpUtil;

/**
 * 对话编排服务
 * 串联 NLP 解析 -> 意图路由 -> 业务查询 -> 自然语言回答 的完整链路
 * 支持多轮对话记忆，同一 sessionId 的历史上下文会传入 LLM
 */
@Slf4j
@Service
public class ConversationOrchestrator {

    private static final String ANSWER_FORMATTER_SYSTEM_PROMPT = """
        你是物流智能平台的智能助手。请根据业务查询结果，用简洁友好的中文回答用户的问题。

        要求：
        1. 直接回答问题，不要重复用户的问题
        2. 使用专业的物流/仓储领域术语
        3. 如果查询结果为空，告知用户未找到相关信息
        4. 适当组织信息，使用分点或分行让回答更清晰
        5. 如果有数值数据，带上合适的单位
        """;

    /** 普通对话提示词：OTHER 意图不走知识库与业务查询，由模型直接自由回答。 */
    private static final String GENERAL_CHAT_SYSTEM_PROMPT = """
        你是物流智能平台的智能助手。请直接、友好地用中文回答用户的问题。

        要求：
        1. 直接回答问题，不要重复用户的问题
        2. 仅基于通用常识回答，不确定的业务数据不要编造，如实说明
        3. 回答简洁清晰，必要时分点说明
        4. 结合对话历史理解上下文，保持多轮对话连贯
        """;

    private final IntentRecognitionService intentService;
    private final IntentRouter intentRouter;
    private final LlmPort llmPort;
    private final ConversationRepository conversationRepository;
    private final ConversationHistoryRepository historyRepository;

    private final StreamingChatModel streamingChatModel;


    /**
     * RAG 知识库问答服务
     * <p>知识库为常驻能力，该依赖由构造器强制注入；若其底层依赖（向量库 / EmbeddingModel）未配置，
     * 应用启动即失败（fail-fast），符合"知识库永远启用"的预期。</p>
     */
    private final KnowledgeQaService knowledgeQaService;

    /**
     * NL2SQL数据库查询服务（可选，启用 database-rag 相关配置时注入）
     */
    @Autowired(required = false)
    private DatabaseRagService databaseRagService;

    /**
     * 构造函数
     *
     * @param chatModel      LangChain4j 聊天模型
     * @param intentService  意图识别服务
     * @param intentRouter   意图路由器
     * @param memoryManager  对话记忆管理器
     */
    public ConversationOrchestrator(IntentRecognitionService intentService,
                                    IntentRouter intentRouter,
                                    ConversationRepository conversationRepository,
                                    LlmPort llmPort,
                                    StreamingChatModel streamingChatModel,
                                    KnowledgeQaService knowledgeQaService,
                                    ConversationHistoryRepository historyRepository) {
        this.intentService = intentService;
        this.intentRouter = intentRouter;
        this.conversationRepository = conversationRepository;
        this.llmPort = llmPort;
        this.streamingChatModel = streamingChatModel;
        this.knowledgeQaService = knowledgeQaService;
        this.historyRepository = historyRepository;
    }

    /**
     * 处理用户自然语言输入（无会话上下文，单轮对话）
     *
     * @param userMessage 用户自然语言输入
     * @return 对话响应，包含 NLP 解析结果和自然语言回答
     */
    public ConversationResponse chat(String userMessage) {
        return chat(userMessage, null);
    }

    /**
     * 处理用户自然语言输入（带会话上下文，支持多轮对话）
     *
     * @param userMessage 用户自然语言输入
     * @param sessionId   会话ID，null 表示单轮对话
     * @return 对话响应，包含 sessionId、NLP 解析结果和自然语言回答
     */
    public ConversationResponse chat(String userMessage, String sessionId) {
        try {
            // 入口（请求线程）获取登录用户ID，向下透传，避免异步落库时 Sa-Token 上下文不可用
            Long userId = getLoginUserId();
            // 1. 获取历史上下文
            List<ChatMessage> history = conversationRepository.getHistory(sessionId);
            log.debug("对话历史: sessionId={}, 轮数={}", sessionId, history.size() / 2);

            // 2. NLP 解析：意图 + 实体
            NlpParseResult parseResult = intentService.parseWithEntities(userMessage);
            log.info("NLP解析结果 - 意图: {}, 实体: {}", parseResult.getIntent(), parseResult.getEntities());

            // 4. KNOWLEDGE_QA 走 RAG 知识库问答流程（不走业务处理器路由）
            if (parseResult.getIntent() == IntentEnum.KNOWLEDGE_QA) {
                String answer = handleKnowledgeQa(userMessage);
                saveMemory(sessionId, userMessage, answer, parseResult, userId);
                return ConversationResponse.builder()
                        .sessionId(sessionId)
                        .intent(IntentEnum.KNOWLEDGE_QA.name())
                        .entities(parseResult.getEntities())
                        .answer(answer)
                        .build();
            }

            // 3. OTHER 意图：识别不出 -> 直接普通对话回答（不检索知识库、不查业务数据）
            if (parseResult.getIntent() == IntentEnum.OTHER) {
                String plainAnswer = llmPort.complete(GENERAL_CHAT_SYSTEM_PROMPT,
                        buildContextHint(history) + userMessage);
                log.info("未知意图普通对话回答: {}", plainAnswer);
                saveMemory(sessionId, userMessage, plainAnswer, parseResult, userId);
                return ConversationResponse.builder()
                        .sessionId(sessionId)
                        .intent(IntentEnum.OTHER.name())
                        .entities(parseResult.getEntities())
                        .answer(plainAnswer)
                        .build();
            }

            // 5. DATABASE_QUERY / DATA_STATISTICS 走 NL2SQL 流程（直接查询数据库）
            if ((parseResult.getIntent() == IntentEnum.DATABASE_QUERY
                    || parseResult.getIntent() == IntentEnum.DATA_STATISTICS)
                    && databaseRagService != null) {
                String nl2sqlAnswer = databaseRagService.query(userMessage);
                log.info("NL2SQL回答: {}", nl2sqlAnswer);
                saveMemory(sessionId, userMessage, nl2sqlAnswer, parseResult, userId);
                return ConversationResponse.builder()
                        .sessionId(sessionId)
                        .intent(parseResult.getIntent().name())
                        .entities(parseResult.getEntities())
                        .answer(nl2sqlAnswer)
                        .build();
            }

            // 6. 其他意图：走标准业务处理器路由
            BizQueryResult bizResult = intentRouter.route(
                    parseResult.getIntent(),
                    userMessage,
                    parseResult.getEntities()
            );
            log.info("业务查询结果: {}", bizResult);

            // 7. LLM 将结构化数据转化为自然语言回答，带上历史上下文
            String contextHint = buildContextHint(history);
            String queryResultJson = JSON.toJSONString(bizResult.getData());
            String naturalAnswer = llmPort.complete(
                    ANSWER_FORMATTER_SYSTEM_PROMPT, contextHint + userMessage + "\n" + queryResultJson);
            log.info("自然语言回答: {}", naturalAnswer);

            saveMemory(sessionId, userMessage, naturalAnswer, parseResult, userId);

            return ConversationResponse.builder()
                    .sessionId(sessionId)
                    .intent(parseResult.getIntent().name())
                    .entities(parseResult.getEntities())
                    .queryResult(bizResult.getData())
                    .answer(naturalAnswer)
                    .build();

        } catch (BusinessException e) {
            log.warn("业务处理异常: {}", e.getMessage());
            return ConversationResponse.builder()
                    .sessionId(sessionId)
                    .answer("抱歉，" + e.getMessage() + "。请提供更多信息。")
                    .build();
        } catch (Exception e) {
            log.error("对话处理异常", e);
            return ConversationResponse.builder()
                    .sessionId(sessionId)
                    .answer("抱歉，处理您的问题时出了点问题，请稍后重试。")
                    .build();
        }
    }

    /**
     * 流式处理用户自然语言输入（带会话上下文，支持SSE流式输出）
     *
     * @param userMessage 用户自然语言输入
     * @param sessionId   会话ID，null 表示单轮对话
     * @return Flux<String> 流式响应片段
     */
    public Flux<String> chatStream(String userMessage, String sessionId) {
        // 入口（请求线程）获取登录用户ID，向下透传，避免流式异步线程 Sa-Token 上下文不可用
        Long userId = getLoginUserId();

        return Flux.<String>defer(() -> {
            try {
                // 1. 获取历史上下文
                List<ChatMessage> history = conversationRepository.getHistory(sessionId);
                log.debug("对话历史: sessionId={}, 轮数={}", sessionId, history.size() / 2);

                // 2. NLP 解析：意图 + 实体
                NlpParseResult parseResult = intentService.parseWithEntities(userMessage);
                log.info("NLP解析结果 - 意图: {}, 实体: {}",
                        parseResult.getIntent(), parseResult.getEntities());

                // 3. OTHER 意图：识别不出 -> 直接普通对话流式回答（不检索知识库、不查业务数据）
                if (parseResult.getIntent() == IntentEnum.OTHER) {
                    return withMemory(plainChatStream(userMessage, history),
                            sessionId, userMessage, parseResult, userId);
                }

                // 4. KNOWLEDGE_QA 走 RAG 知识库问答流程（不走业务处理器路由）
                if (parseResult.getIntent() == IntentEnum.KNOWLEDGE_QA) {
                    return withMemory(knowledgeQaService.answerStream(userMessage),
                            sessionId, userMessage, parseResult, userId);
                }

                // 5. DATABASE_QUERY / DATA_STATISTICS 走 NL2SQL 流程（直接查询数据库）
                if ((parseResult.getIntent() == IntentEnum.DATABASE_QUERY
                        || parseResult.getIntent() == IntentEnum.DATA_STATISTICS)
                        && databaseRagService != null) {
                    String nl2sqlAnswer = databaseRagService.query(userMessage);
                    log.info("NL2SQL回答: {}", nl2sqlAnswer);
                    saveMemory(sessionId, userMessage, nl2sqlAnswer, parseResult, userId);
                    return Flux.just(nl2sqlAnswer);
                }

                // 6. 其他意图：走标准业务处理器路由
                BizQueryResult bizResult = intentRouter.route(
                        parseResult.getIntent(),
                        userMessage,
                        parseResult.getEntities()
                );
                log.info("业务查询结果: {}", bizResult);

                // 7. LLM 将结构化数据转化为自然语言流式回答，带上历史上下文
                String contextHint = buildContextHint(history);
                String queryResultJson = JSON.toJSONString(bizResult.getData());
                String prompt = contextHint + userMessage + "\n\n查询结果数据：" + queryResultJson;

                List<ChatMessage> messages = new ArrayList<>(history);
                messages.add(UserMessage.from(prompt));

                return Flux.<String>create(sink -> {
                    StringBuilder fullAnswer = new StringBuilder();
                    // 流式句柄仅在首个 token 到达后可用，用 AtomicReference 在回调与取消信号间传递
                    AtomicReference<StreamingHandle> handleRef = new AtomicReference<>();
                    sink.onCancel(() -> {
                        StreamingCancellation.cancel(handleRef, "对话");
                        savePartialMemory(sessionId, userMessage, fullAnswer, parseResult, userId);
                    });

                    streamingChatModel.chat(messages, new StreamingChatResponseHandler() {
                        @Override
                        public void onPartialResponse(String token) {
                            fullAnswer.append(token);
                            sink.next(token);
                        }

                        @Override
                        public void onPartialResponse(PartialResponse partialResponse,
                                                     PartialResponseContext context) {
                            // 两个重载都覆盖：provider 走带 context 的路径时可拿到取消句柄，
                            // 走旧 String 路径时也能输出 token；token 统一由 String 重载推送，避免重复
                            handleRef.compareAndSet(null, context.streamingHandle());
                            onPartialResponse(partialResponse.text());
                        }

                        @Override
                        public void onCompleteResponse(ChatResponse completeResponse) {
                            saveMemory(sessionId, userMessage, fullAnswer.toString(),
                                    parseResult, userId);
                            sink.complete();
                        }

                        @Override
                        public void onError(Throwable error) {
                            log.error("流式回答生成异常", error);
                            sink.error(error);
                        }
                    });
                });

            } catch (BusinessException e) {
                log.warn("业务处理异常: {}", e.getMessage());
                return Flux.just("抱歉，" + e.getMessage() + "。请提供更多信息。");
            } catch (Exception e) {
                log.error("对话处理异常", e);
                return Flux.<String>error(e);
            }
        });
    }

    /**
     * 保存被中断时已输出的部分回答
     * <p>调用 cancel() 后框架不会再触发 onCompleteResponse，若不在此处补写，
     * 用户已看到的那半段回答不会进入对话记忆，下一轮会出现上下文失忆。</p>
     *
     * @param sessionId    会话ID
     * @param userMessage  用户消息
     * @param fullAnswer   已输出的回答片段
     * @param parseResult  NLP解析结果
     * @param userId       登录用户ID
     */
    private void savePartialMemory(String sessionId, String userMessage,
                                   StringBuilder fullAnswer, NlpParseResult parseResult,
                                   Long userId) {
        if (fullAnswer.length() == 0) {
            return;
        }
        log.info("SSE 客户端断开，保存部分回答: sessionId={}, length={}",
                sessionId, fullAnswer.length());
        saveMemory(sessionId, userMessage, fullAnswer.toString(), parseResult, userId);
    }

    /**
     * 流式响应记忆包装器
     * <p>主路径为流式输出，需在 token 流结束后把完整答案落对话记忆，
     * 以支持多轮上下文。用户消息与完整回答一并保存。</p>
     * <p>客户端断开时 doOnComplete 不会触发，改为保存已输出的部分回答，
     * 避免下一轮对话出现上下文失忆。</p>
     *
     * @param source      原始流式 token 源
     * @param sessionId   会话ID，null 不保存
     * @param userMessage 用户消息
     * @param parseResult NLP解析结果
     * @param userId      登录用户ID
     * @return 透传的流式 token 源（仅叠加记忆副作用）
     */
    private Flux<String> withMemory(Flux<String> source, String sessionId, String userMessage,
                                    NlpParseResult parseResult, Long userId) {
        StringBuilder fullAnswer = new StringBuilder();
        return source
                .doOnNext(fullAnswer::append)
                .doOnComplete(() -> saveMemory(sessionId, userMessage,
                        fullAnswer.toString(), parseResult, userId))
                .doOnCancel(() -> savePartialMemory(sessionId, userMessage,
                        fullAnswer, parseResult, userId))
                .doOnError(e -> log.error("流式回答生成异常", e));
    }

    /**
     * 普通对话流式回答。
     * <p>OTHER 意图专用：不检索知识库、不查业务数据，仅把历史上下文与用户消息交给模型自由回答。
     * 客户端断开时通过底层流式句柄取消生成，避免断开后模型继续占用算力；
     * 完整/部分回答的记忆落库由外层 withMemory 统一处理。</p>
     *
     * @param userMessage 用户消息
     * @param history     多轮对话历史
     * @return 流式 token 源
     */
    private Flux<String> plainChatStream(String userMessage, List<ChatMessage> history) {
        List<ChatMessage> messages = new ArrayList<>(history);
        messages.add(UserMessage.from(GENERAL_CHAT_SYSTEM_PROMPT + "\n\n"
                + buildContextHint(history) + userMessage));

        return Flux.<String>create(sink -> {
            // 流式句柄仅在首个 token 到达后可用，用 AtomicReference 在回调与取消信号间传递
            AtomicReference<StreamingHandle> handleRef = new AtomicReference<>();
            sink.onCancel(() -> StreamingCancellation.cancel(handleRef, "普通对话"));

            streamingChatModel.chat(messages, new StreamingChatResponseHandler() {
                @Override
                public void onPartialResponse(String token) {
                    sink.next(token);
                }

                @Override
                public void onPartialResponse(PartialResponse partialResponse,
                                             PartialResponseContext context) {
                    // 两个重载都覆盖：provider 走带 context 的路径时可拿到取消句柄，
                    // 走旧 String 路径时也能输出 token；token 统一由 String 重载推送，避免重复
                    handleRef.compareAndSet(null, context.streamingHandle());
                    onPartialResponse(partialResponse.text());
                }

                @Override
                public void onCompleteResponse(ChatResponse completeResponse) {
                    sink.complete();
                }

                @Override
                public void onError(Throwable error) {
                    log.error("普通对话流式回答异常", error);
                    sink.error(error);
                }
            });
        });
    }

    /**
     * 清除指定会话的对话记忆
     *
     * @param sessionId 会话ID
     */
    public void clearMemory(String sessionId) {
        conversationRepository.clear(sessionId);
    }

    /**
     * 处理知识库问答意图
     */
    private String handleKnowledgeQa(String userMessage) {
        String answer = knowledgeQaService.answer(userMessage);
        log.info("RAG知识库回答: {}", answer);
        return answer;
    }

    /**
     * 保存对话记忆（用户消息 + AI回答）并落库历史记录
     *
     * @param sessionId   会话ID，null 则不保存
     * @param userMessage 用户消息
     * @param aiAnswer    AI回答
     * @param parseResult NLP解析结果（提供意图与实体）
     */
    private void saveMemory(String sessionId, String userMessage, String aiAnswer,
                            NlpParseResult parseResult, Long userId) {
        if (sessionId == null || sessionId.isBlank()) {
            return;
        }
        // 1. 写入 Redis 多轮对话记忆，供模型上下文使用
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(UserMessage.from(userMessage));
        messages.add(AiMessage.from(aiAnswer));
        conversationRepository.appendMessages(sessionId, messages);
        // 2. 落库 MySQL 持久化历史记录，供用户侧查看历史会话
        persistRecord(sessionId, userMessage, aiAnswer, parseResult, userId);
    }

    /**
     * 将一轮对话持久化到 MySQL（父子表）
     * <p>父表维护会话汇总信息，子表写入 user + assistant 两条消息，userId 取自 Sa-Token 登录态。</p>
     *
     * @param sessionId   会话ID（即 conversation_id）
     * @param userMessage 用户消息
     * @param aiAnswer    AI回答
     * @param parseResult NLP解析结果
     */
    private void persistRecord(String sessionId, String userMessage, String aiAnswer,
                               NlpParseResult parseResult, Long userId) {
        try {
            // userId 由请求线程入口透传，避免流式异步线程中 Sa-Token 上下文不可用
            historyRepository.saveRound(sessionId, userId, userMessage, aiAnswer, parseResult, 0, 0);
        } catch (Exception e) {
            // 历史落库失败不应影响主对话链路
            log.error("对话历史落库失败，conversationId={}：{}", sessionId, e.getMessage());
        }
    }

    /**
     * 在请求线程入口安全获取当前登录用户ID
     * <p>Sa-Token 上下文基于 ThreadLocal，仅请求线程可用；未登录返回 null（不落库 userId）。</p>
     *
     * @return 用户ID，未登录为 null
     */
    private Long getLoginUserId() {
        try {
            return StpUtil.getLoginIdAsLong();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 构建历史上下文提示，让 LLM 知道之前的对话内容
     * 取最近6条（3轮对话）做摘要，避免 token 过长
     *
     * @param history 历史消息列表
     * @return 上下文提示文本
     */
    private String buildContextHint(List<ChatMessage> history) {
        if (history == null || history.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("【对话历史摘要】\n");
        int start = Math.max(0, history.size() - 6);
        for (int i = start; i < history.size(); i++) {
            ChatMessage msg = history.get(i);
            if (msg instanceof UserMessage) {
                sb.append("用户: ").append(((UserMessage) msg).singleText()).append("\n");
            } else if (msg instanceof AiMessage) {
                sb.append("助手: ").append(((AiMessage) msg).text()).append("\n");
            }
        }
        sb.append("【当前问题】\n");
        return sb.toString();
    }

}
