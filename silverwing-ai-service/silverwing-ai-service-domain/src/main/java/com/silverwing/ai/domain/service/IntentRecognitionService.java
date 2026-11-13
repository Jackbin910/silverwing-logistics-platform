package com.silverwing.ai.domain.service;

import com.silverwing.ai.domain.model.EntityResult;
import com.silverwing.ai.domain.model.NlpParseResult;
import com.silverwing.biz.ai.domain.enums.EntityTypeEnum;
import com.silverwing.biz.ai.domain.enums.IntentEnum;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.router.ChatModelRoute;
import dev.langchain4j.model.chat.router.ChatModelRouter;
import dev.langchain4j.model.chat.router.ChatModelRoutingRequest;
import dev.langchain4j.model.chat.router.ChatModelRoutingResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 意图识别服务实现
 * 使用 LangChain4j 决策模型（nimble）做意图分类（低置信度兜底 OTHER），
 * 实体通过正则从用户输入中提取，避免重型大模型生成 JSON。
 */
@Slf4j
@Service
public class IntentRecognitionService {

    private static final double DEFAULT_CONFIDENCE = 0.8D;

    /** 意图分类命中时赋予的置信度（决策模型不暴露具体概率，命中即视为高置信）。 */
    private static final double CLASSIFY_HIT_CONFIDENCE = 0.85D;

    /** 设备编码，如 AGV-002、ROBOT-001、DOG-003。 */
    private static final Pattern DEVICE_ID_PATTERN =
            Pattern.compile("(?i)\\b[A-Z]{2,}-\\d{2,}\\b");

    /** 订单号，如 ORD-20260330-001。 */
    private static final Pattern ORDER_NO_PATTERN =
            Pattern.compile("(?i)\\bORD-[0-9-]+\\b");

    /** 库位编号，如 12-1-1、3-2-5；前后排除数字，避免误匹配日期。 */
    private static final Pattern BIN_LOCATION_PATTERN =
            Pattern.compile("(?<!\\d)\\d{1,3}-\\d{1,3}-\\d{1,3}(?!\\d)");

    /** 常见设备指标关键词。 */
    private static final List<String> METRICS = List.of(
            "运行时长", "温度", "电量", "速度", "振动", "里程", "电压", "电流", "在线", "状态");

    /** 意图分类候选：除 OTHER 外的全部意图，route 名即意图编码。 */
    private static final List<ChatModelRoute> INTENT_ROUTES = buildIntentRoutes();

    private final ChatModelRouter intentClassifier;

    /**
     * 构造函数，注入意图分类器。
     *
     * @param intentClassifier 基于决策模型的意图分类器
     */
    public IntentRecognitionService(
            @Qualifier("intentClassifier") ChatModelRouter intentClassifier) {
        this.intentClassifier = intentClassifier;
    }

    /**
     * 从用户输入中提取实体。
     *
     * @param userMessage 用户自然语言输入
     * @return 实体列表
     */
    public List<EntityResult> extractEntities(String userMessage) {
        return parseWithEntities(userMessage).getEntities();
    }

    /**
     * 一次调用同时完成意图识别和实体提取。
     *
     * @param userMessage 用户自然语言输入
     * @return 综合解析结果
     */
    public NlpParseResult parseWithEntities(String userMessage) {
        try {
            IntentEnum intent = classifyIntent(userMessage);
            List<EntityResult> entities = extractEntitiesByRule(userMessage);
            double confidence = intent == IntentEnum.OTHER ? 0.0D : CLASSIFY_HIT_CONFIDENCE;
            NlpParseResult result = buildParseResult(intent, entities, confidence);
            log.info("统一NLP解析结果: intent={}, entities={} <- \"{}\"",
                    intent.getCode(), entities, userMessage);
            return result;
        } catch (Exception e) {
            log.error("NLP综合解析失败: {}", userMessage, e);
            return buildParseResult(IntentEnum.OTHER, new ArrayList<>(), 0.0D);
        }
    }

    /**
     * 使用决策模型对用户消息做意图分类。
     *
     * @param userMessage 用户自然语言输入
     * @return 识别出的意图，低置信度或无法识别时返回 OTHER
     */
    private IntentEnum classifyIntent(String userMessage) {
        ChatModelRoutingRequest request = ChatModelRoutingRequest.builder()
                .chatRequest(ChatRequest.builder()
                        .messages(UserMessage.from(userMessage))
                        .build())
                .routes(INTENT_ROUTES)
                .build();
        ChatModelRoutingResult result = intentClassifier.route(request);

        // 低置信度或分类失败时决策模型返回默认路由，映射为 OTHER
        if (result.isDefaultRoute()) {
            return IntentEnum.OTHER;
        }
        IntentEnum intent = IntentEnum.getByCode(result.routeName());
        return intent == null ? IntentEnum.OTHER : intent;
    }

    /**
     * 通过正则规则从用户输入中提取实体。
     *
     * @param userMessage 用户自然语言输入
     * @return 实体列表
     */
    private List<EntityResult> extractEntitiesByRule(String userMessage) {
        List<EntityResult> entities = new ArrayList<>();
        extractByPattern(entities, EntityTypeEnum.DEVICE_ID, DEVICE_ID_PATTERN, userMessage);
        extractByPattern(entities, EntityTypeEnum.ORDER_NO, ORDER_NO_PATTERN, userMessage);
        extractByPattern(entities, EntityTypeEnum.BIN_LOCATION, BIN_LOCATION_PATTERN, userMessage);

        // 指标名按关键词匹配
        for (String metric : METRICS) {
            if (userMessage.contains(metric)) {
                addEntity(entities, EntityTypeEnum.METRIC, metric);
            }
        }
        return entities;
    }

    /**
     * 按正则提取实体并去重追加。
     *
     * @param entities 实体列表
     * @param type     实体类型
     * @param pattern  匹配模式
     * @param text     待匹配文本
     */
    private void extractByPattern(List<EntityResult> entities, EntityTypeEnum type,
                                  Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            addEntity(entities, type, matcher.group());
        }
    }

    /**
     * 追加实体，避免重复值。
     *
     * @param entities 实体列表
     * @param type     实体类型
     * @param value    实体值
     */
    private void addEntity(List<EntityResult> entities, EntityTypeEnum type, String value) {
        boolean exists = entities.stream()
                .anyMatch(e -> e.getType() == type && value.equals(e.getValue()));
        if (!exists) {
            entities.add(EntityResult.builder().type(type).value(value).build());
        }
    }

    /**
     * 构建意图分类候选路由。
     *
     * @return 除 OTHER 外的意图路由列表
     */
    private static List<ChatModelRoute> buildIntentRoutes() {
        List<ChatModelRoute> routes = new ArrayList<>();
        for (IntentEnum intent : IntentEnum.values()) {
            if (intent == IntentEnum.OTHER) {
                continue;
            }
            // 名称 + 详细描述，帮助决策模型区分相近意图
            routes.add(new ChatModelRoute(intent.getCode(),
                    intent.getName() + "：" + intent.getDescription()));
        }
        return routes;
    }

    /**
     * 构建统一解析结果。
     *
     * @param intent     意图
     * @param entities   实体列表
     * @param confidence 置信度
     * @return 统一解析结果
     */
    private NlpParseResult buildParseResult(IntentEnum intent,
                                            List<EntityResult> entities,
                                            Double confidence) {
        return NlpParseResult.builder()
                .intent(intent)
                .entities(entities == null ? new ArrayList<>() : entities)
                .confidence(normalizeConfidence(confidence))
                .build();
    }

    /**
     * 规范化置信度。
     *
     * @param confidence 原始置信度
     * @return 合法的置信度
     */
    private Double normalizeConfidence(Double confidence) {
        // NaN / Infinity 与 0、1 的比较恒为 false，需显式拦截，否则非法置信度会透传给模型
        if (confidence == null || confidence.isNaN() || confidence.isInfinite()
                || confidence < 0.0D || confidence > 1.0D) {
            return DEFAULT_CONFIDENCE;
        }
        return confidence;
    }
}
