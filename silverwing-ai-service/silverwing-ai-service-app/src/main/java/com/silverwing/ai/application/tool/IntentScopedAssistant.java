package com.silverwing.ai.application.tool;

import com.silverwing.ai.domain.model.EntityResult;
import com.silverwing.ai.domain.model.NlpParseResult;
import com.silverwing.ai.domain.service.IntentRecognitionService;
import com.silverwing.biz.ai.domain.enums.IntentEnum;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 意图限定式智能助手（意图路由 + Tool Agent 混合模式，流式 + 记忆）
 *
 * <p>设计目标：保留意图识别的确定性与可控性，同时让 LLM 在"意图域"内自主调用工具执行，
 * 兼顾可控与灵活。与纯意图路由（IntentRouter + Handler）和纯自由 Agent 的区别：</p>
 * <ul>
 *     <li>纯意图路由：一个意图硬编码对应一个 Handler，无 LLM 工具调用；</li>
 *     <li>纯自由 Agent：LLM 在所有工具中自由发挥，可控性差；</li>
 *     <li>本模式：先由 {@link IntentRecognitionService} 定下意图（确定性边界），
 *     再只把该意图相关的 tool 暴露给 LLM，从工具层面强制不越界，
 *     并把已识别的意图与预提取实体注入消息，约束 LLM 按意图、用正确参数执行。</li>
 * </ul>
 *
 * <p>流式与记忆：本助手基于 {@link StreamingChatModel} + {@link Flux} 实现逐 token 流式输出
 * （参考 RAG 知识问答的响应式流式风格，由 AiServices 原生支持返回 Flux）；
 * 多轮记忆复用项目统一的 Redis {@link ChatMemoryStore}，通过 {@link ChatMemoryProvider}
 * 按 sessionId 隔离上下文。记忆存储与 {@code ConversationOrchestrator} 共用同一 Redis 空间，
 * 因此 {@code /chat/memory/{sessionId}} 清记忆端点对 Agent 模式同样生效。</p>
 */
@Slf4j
@Component
public class IntentScopedAssistant {

    /**
     * 单次会话记忆保留的最大消息数，与 ConversationRepositoryImpl 保持一致
     */
    private static final int MAX_MESSAGES = 20;

    private final IntentRecognitionService intentService;
    /**
     * 统一的记忆存储（Redis），保证 /chat 与 /chat/agent 共享同一会话空间
     */
    private final ChatMemoryStore chatMemoryStore;
    /**
     * 按意图预构建的"仅含相关工具"的流式助手，从工具层面限制 LLM 不越界
     */
    private final Map<IntentEnum, StreamingAssistant> scopedAssistants;
    /**
     * 全量助手：兜底未明确归类的意图（LLM 可在全部工具中自选）
     */
    private final StreamingAssistant fullAssistant;

    /**
     * 构造函数，注入流式模型、记忆存储、意图识别服务与全部工具实例，并预构建意图域助手。
     *
     * @param streamingChatModel 流式聊天模型
     * @param chatMemoryStore    Redis 记忆存储（与全局对话记忆共用）
     * @param intentService      意图识别服务
     * @param deviceTools        设备查询工具
     * @param orderTools         订单查询工具
     * @param workOrderTools     工单工具
     * @param opsTools            运维工具（H800 开门等）
     */
    public IntentScopedAssistant(
            StreamingChatModel streamingChatModel,
            ChatMemoryStore chatMemoryStore,
            IntentRecognitionService intentService,
            DeviceTools deviceTools,
            OrderTools orderTools,
            WorkOrderTools workOrderTools,
            OpsTools opsTools) {
        this.intentService = intentService;
        this.chatMemoryStore = chatMemoryStore;

        // 按 sessionId 提供记忆，复用统一 Redis 存储，实现多轮流式对话记忆
        ChatMemoryProvider memoryProvider = this::buildMemory;

        // 全量助手：兜底未明确归类的意图
        this.fullAssistant = buildAssistant(streamingChatModel, memoryProvider,
                deviceTools, orderTools, workOrderTools, opsTools);

        // 按意图预构建"仅含相关工具"的助手，从工具层面强制 LLM 不越界
        Map<IntentEnum, StreamingAssistant> map = new EnumMap<>(IntentEnum.class);
        for (IntentEnum intent : deviceIntents()) {
            map.put(intent, buildAssistant(streamingChatModel, memoryProvider, deviceTools));
        }
        map.put(IntentEnum.QUERY_ORDER_STATUS, buildAssistant(streamingChatModel, memoryProvider, orderTools));
        map.put(IntentEnum.CREATE_WORK_ORDER, buildAssistant(streamingChatModel, memoryProvider, workOrderTools));
        map.put(IntentEnum.MAINTENANCE_ASSIST, buildAssistant(streamingChatModel, memoryProvider, workOrderTools));
        map.put(IntentEnum.FAULT_REPORT, buildAssistant(streamingChatModel, memoryProvider, workOrderTools));
        map.put(IntentEnum.OPEN_WAREHOUSE, buildAssistant(streamingChatModel, memoryProvider, opsTools));
        this.scopedAssistants = map;
    }

    /**
     * 构建仅暴露指定工具的流式 AiServices 助手（带多轮记忆）
     * <p>1.x 中 ChatModel 与 StreamingChatModel 是两个独立接口；接口方法返回 Flux 时，
     * 必须调用 {@code .streamingChatModel(...)} 让 AiServices 走流式通道。</p>
     *
     * @param streamingChatModel 流式聊天模型
     * @param memoryProvider     记忆提供器（按 sessionId 隔离）
     * @param tools              该意图域允许暴露的工具实例
     * @return 限定工具的流式助手
     */
    private static StreamingAssistant buildAssistant(
            StreamingChatModel streamingChatModel,
            ChatMemoryProvider memoryProvider,
            Object... tools) {
        return AiServices.builder(StreamingAssistant.class)
                .streamingChatModel(streamingChatModel)
                .chatMemoryProvider(memoryProvider)
                .tools(tools)
                .build();
    }

    /**
     * 根据会话ID构建记忆窗口，复用统一 Redis 存储，保证清记忆端点可穿透生效
     *
     * @param memoryId 会话ID（由 {@link MemoryId} 参数传入）
     * @return 该会话的滑动窗口记忆
     */
    private ChatMemory buildMemory(Object memoryId) {
        return MessageWindowChatMemory.builder()
                .id(String.valueOf(memoryId))
                .chatMemoryStore(chatMemoryStore)
                .maxMessages(MAX_MESSAGES)
                .build();
    }

    /**
     * 设备类意图集合（共用 DeviceTools 工具域）
     *
     * @return 设备相关意图列表
     */
    private static List<IntentEnum> deviceIntents() {
        return List.of(
                IntentEnum.QUERY_DEVICE_STATUS,
                IntentEnum.QUERY_DEVICE_LOCATION,
                IntentEnum.QUERY_DEVICE_METRIC);
    }

    /**
     * 流式处理用户对话（带会话记忆）：先识别意图，再在与该意图对应的工具域内由 LLM 自主调用工具执行。
     * 逐 token 返回，并自动维护多轮上下文（记忆写入由 AiServices 经 ChatMemoryStore 完成）。
     *
     * @param userMessage 用户输入的自然语言
     * @param sessionId   会话ID，用于隔离与恢复多轮上下文
     * @return 逐 token 的流式文本片段（Flux）
     */
    public Flux<String> chatStream(String userMessage, String sessionId) {
        log.info("意图限定 Agent 流式处理: sessionId={}, message={}", sessionId, userMessage);
        NlpParseResult parse = intentService.parseWithEntities(userMessage);
        IntentEnum intent = parse.getIntent();

        StreamingAssistant assistant = scopedAssistants.get(intent);
        if (assistant == null) {
            log.info("意图 {} 无专用工具域，回落全量助手", intent);
            assistant = fullAssistant;
        }

        // 注入已识别意图与预提取实体，约束 LLM 按意图、用正确参数执行
        String augmentedMessage = buildAugmentedMessage(userMessage, parse);

        return assistant.chat(sessionId, augmentedMessage)
                .doOnComplete(() -> log.info("意图限定 Agent 流式完成: intent={}", intent))
                .doOnError(e -> log.error("意图限定 Agent 流式异常: intent={}", intent, e));
    }

    /**
     * 将已识别的意图与预提取实体拼接到用户消息前，作为执行约束。
     *
     * @param userMessage 原始用户消息
     * @param parse       意图识别结果（含意图、置信度、实体）
     * @return 注入约束后的消息
     */
    private String buildAugmentedMessage(String userMessage, NlpParseResult parse) {
        StringBuilder sb = new StringBuilder();
        sb.append("[系统已识别意图=").append(parse.getIntent().getCode())
                .append("，置信度=").append(parse.getConfidence());
        List<EntityResult> entities = parse.getEntities();
        if (entities != null && !entities.isEmpty()) {
            sb.append("；预提取实体=");
            for (int i = 0; i < entities.size(); i++) {
                if (i > 0) {
                    sb.append("，");
                }
                sb.append(entities.get(i).getType()).append("=").append(entities.get(i).getValue());
            }
        }
        sb.append("]\n").append(userMessage);
        return sb.toString();
    }

    /**
     * 流式智能助手接口（意图限定 Agent 专用）
     * <p>通过 {@link MemoryId} 声明会话隔离键，配合 {@link ChatMemoryProvider} 实现多轮记忆；
     * 返回 {@link Flux} 以支持逐 token 响应式流式输出（参考 RAG 知识问答的流式风格）。
     * 系统提示词强调"只能在当前可用工具中执行"，与工具域限定的强约束一致。</p>
     */
    @SystemMessage("""
            你是银翼物流平台的智能助手"银翼小助手"。

            当前对话仅向你开放了与用户意图相关的部分工具，你只能在提供给你的工具列表中执行动作或查询。
            不要假设存在其它未提供的工具。

            当用户提出问题时，你应该：
            1. 准确理解用户的意图（系统已在消息前缀标注了已识别意图与预提取实体）
            2. 调用合适的工具获取信息或执行动作
            3. 基于工具返回的结果，用简洁、专业、友好的中文回复用户

            关于开门（控制设备）的重要说明：
            - 当用户表达开门意图时，应立即调用 openWarehouse 工具，参数 location 填写用户提到的库位编号（形如 12-1-1）
            - 开门是真实设备动作，执行后请如实告知用户成功或失败的结果

            注意：
            - 如果用户没有提供足够的信息（如设备编码、库位编号），请先询问
            - 如果查询结果为空，请明确告知用户
            - 保持回答简洁、专业、易懂
            """)
    public interface StreamingAssistant {
        /**
         * 流式对话
         *
         * @param sessionId   会话ID，用于隔离多轮记忆
         * @param userMessage 用户消息（已注入意图与实体约束）
         * @return 逐 token 流式输出
         */
        Flux<String> chat(@MemoryId String sessionId, @UserMessage String userMessage);
    }
}
