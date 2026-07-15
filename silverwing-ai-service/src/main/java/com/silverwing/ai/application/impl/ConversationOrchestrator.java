package com.silverwing.ai.application.impl;

import com.alibaba.fastjson2.JSON;
import com.silverwing.ai.application.dto.*;
import com.silverwing.biz.ai.domain.enums.IntentEnum;
import com.silverwing.ai.application.ai.AnswerFormatter;
import com.silverwing.ai.application.rag.DatabaseRagService;
import com.silverwing.ai.application.rag.KnowledgeQaService;
import com.silverwing.common.exception.BusinessException;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 对话编排服务
 * 串联 NLP 解析 -> 意图路由 -> 业务查询 -> 自然语言回答 的完整链路
 * 支持多轮对话记忆，同一 sessionId 的历史上下文会传入 LLM
 */
@Slf4j
@Service
public class ConversationOrchestrator {

    private final IntentRecognitionService intentService;
    private final IntentRouter intentRouter;
    private final AnswerFormatter answerFormatter;
    private final ConversationMemoryManager memoryManager;

    /**
     * RAG 知识库问答服务（可选，启用 langchain4j.rag.enabled=true 时注入）
     */
    @Autowired(required = false)
    private KnowledgeQaService knowledgeQaService;

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
    public ConversationOrchestrator(ChatModel chatModel,
                                    IntentRecognitionService intentService,
                                    IntentRouter intentRouter,
                                    ConversationMemoryManager memoryManager) {
        this.intentService = intentService;
        this.intentRouter = intentRouter;
        this.memoryManager = memoryManager;
        this.answerFormatter = AiServices.create(AnswerFormatter.class, chatModel);
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
            // 1. 获取历史上下文
            List<ChatMessage> history = memoryManager.getHistory(sessionId);
            log.debug("对话历史: sessionId={}, 轮数={}", sessionId, history.size() / 2);

            // 2. NLP 解析：意图 + 实体
            NlpParseResult parseResult = intentService.parseWithEntities(userMessage);
            log.info("NLP解析结果 - 意图: {}, 实体: {}", parseResult.getIntent(), parseResult.getEntities());

            // 3. OTHER 意图：直接返回通用帮助提示
            if (parseResult.getIntent() == IntentEnum.OTHER) {
                String fallbackAnswer = buildFallbackAnswer(history);
                saveMemory(sessionId, userMessage, fallbackAnswer);
                return ConversationResponse.builder()
                        .sessionId(sessionId)
                        .intent(IntentEnum.OTHER.name())
                        .entities(parseResult.getEntities())
                        .answer(fallbackAnswer)
                        .build();
            }

            // 4. KNOWLEDGE_QA 走 RAG 知识库问答流程（不走业务处理器路由）
            if (parseResult.getIntent() == IntentEnum.KNOWLEDGE_QA) {
                String answer = handleKnowledgeQa(userMessage);
                saveMemory(sessionId, userMessage, answer);
                return ConversationResponse.builder()
                        .sessionId(sessionId)
                        .intent(IntentEnum.KNOWLEDGE_QA.name())
                        .entities(parseResult.getEntities())
                        .answer(answer)
                        .build();
            }

            // 5. DATABASE_QUERY / DATA_STATISTICS 走 NL2SQL 流程（直接查询数据库）
            if ((parseResult.getIntent() == IntentEnum.DATABASE_QUERY
                    || parseResult.getIntent() == IntentEnum.DATA_STATISTICS)
                    && databaseRagService != null) {
                String nl2sqlAnswer = databaseRagService.query(userMessage);
                log.info("NL2SQL回答: {}", nl2sqlAnswer);
                saveMemory(sessionId, userMessage, nl2sqlAnswer);
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
            String naturalAnswer = answerFormatter.format(contextHint + userMessage, queryResultJson);
            log.info("自然语言回答: {}", naturalAnswer);

            saveMemory(sessionId, userMessage, naturalAnswer);

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
     * 清除指定会话的对话记忆
     *
     * @param sessionId 会话ID
     */
    public void clearMemory(String sessionId) {
        memoryManager.clear(sessionId);
    }

    /**
     * 处理知识库问答意图
     */
    private String handleKnowledgeQa(String userMessage) {
        if (knowledgeQaService == null) {
            log.warn("知识库问答功能未启用，请设置 langchain4j.rag.enabled=true");
            return "抱歉，知识库问答功能尚未启用。请联系管理员配置后重试。";
        }
        String answer = knowledgeQaService.answer(userMessage);
        log.info("RAG知识库回答: {}", answer);
        return answer;
    }

    /**
     * 保存对话记忆（用户消息 + AI回答）
     *
     * @param sessionId   会话ID，null 则不保存
     * @param userMessage 用户消息
     * @param aiAnswer    AI回答
     */
    private void saveMemory(String sessionId, String userMessage, String aiAnswer) {
        if (sessionId != null && !sessionId.isBlank()) {
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(UserMessage.from(userMessage));
            messages.add(AiMessage.from(aiAnswer));
            memoryManager.appendMessages(sessionId, messages);
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

    /**
     * OTHER 意图的兜底回答
     * 有历史上下文时给出更简洁的提示，首次对话给出完整帮助
     *
     * @param history 历史消息列表
     * @return 兜底回答
     */
    private String buildFallbackAnswer(List<ChatMessage> history) {
        if (history.isEmpty()) {
            return "抱歉，我暂时无法理解您的问题。您可以试试：\n" +
                    "- 查询设备位置：如\"AGV-002在哪\"\n" +
                    "- 查询设备状态：如\"AGV-002状态如何\"\n" +
                    "- 查询设备指标：如\"AGV-002的温度是多少\"\n" +
                    "- 报告故障：如\"AGV-002充不进电\"\n" +
                    "- 查询订单：如\"ORD-001到哪了\"";
        }
        return "抱歉，我没有完全理解您的意思。您可以试试更具体的描述，" +
                "比如设备编码、订单号等，我可以帮您查询相关信息。";
    }
}
