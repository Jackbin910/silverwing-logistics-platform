package com.silverwing.ai.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.silverwing.ai.dto.EntityResult;
import com.silverwing.ai.dto.NlpParseResult;
import com.silverwing.biz.ai.domain.enums.EntityTypeEnum;
import com.silverwing.biz.ai.domain.enums.IntentEnum;
import com.silverwing.ai.service.ai.UnifiedNlpExtractor;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 意图识别服务实现
 * 通过 LangChain4j AiServices 调用 LLM 进行意图识别和实体提取
 */
@Slf4j
@Service
public class IntentRecognitionService {

    private static final int MAX_ATTEMPTS = 2;
    private static final double DEFAULT_CONFIDENCE = 0.8D;

    private final UnifiedNlpExtractor unifiedNlpExtractor;

    /**
     * 构造函数，注入 ChatLanguageModel 并创建 AI Service 代理
     *
     * @param chatModel LangChain4j 聊天模型
     */
    public IntentRecognitionService(ChatModel chatModel) {
        this.unifiedNlpExtractor = AiServices.create(UnifiedNlpExtractor.class, chatModel);
    }

    /**
     * 识别用户输入的意图。
     *
     * @param userMessage 用户自然语言输入
     * @return 意图枚举
     */
    public IntentEnum recognize(String userMessage) {
        return parseWithEntities(userMessage).getIntent();
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
            NlpParseResult unifiedResult = parseUnifiedWithRetry(userMessage);
            if (unifiedResult != null) {
                log.info("统一NLP解析结果: {} <- \"{}\"",
                        JSON.toJSONString(unifiedResult), userMessage);
                return unifiedResult;
            }

            log.error("统一NLP解析失败，返回兜底结果: {}", userMessage);
            return buildParseResult(IntentEnum.OTHER, new ArrayList<>(), 0.0D);
        } catch (Exception e) {
            log.error("NLP综合解析失败: {}", userMessage, e);
            return buildParseResult(IntentEnum.OTHER, new ArrayList<>(), 0.0D);
        }
    }

    /**
     * 重试执行统一 NLP 解析。
     *
     * @param userMessage 用户自然语言输入
     * @return 统一解析结果，失败时返回 null
     */
    private NlpParseResult parseUnifiedWithRetry(String userMessage) {
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            String rawResult = null;
            try {
                rawResult = unifiedNlpExtractor.extract(userMessage);
                return parseUnifiedResult(rawResult);
            } catch (Exception e) {
                log.warn("第{}次统一NLP解析失败，原始返回: {}", attempt, rawResult, e);
            }
        }
        return null;
    }

    /**
     * 解析统一 NLP JSON 结果。
     *
     * @param jsonStr LLM 返回的 JSON 字符串
     * @return 综合解析结果
     */
    private NlpParseResult parseUnifiedResult(String jsonStr) {
        String jsonContent = extractJsonSegment(jsonStr, '{', '}');
        JSONObject jsonObject = JSON.parseObject(jsonContent);
        if (jsonObject == null) {
            throw new IllegalArgumentException("统一NLP解析结果为空");
        }

        String intentCode = normalizeIntentCode(jsonObject.getString("intent"));
        if (!isValidIntentCode(intentCode)) {
            throw new IllegalArgumentException("统一NLP解析意图非法: " + jsonStr);
        }

        List<EntityResult> entities = parseEntityArray(jsonObject.getJSONArray("entities"));
        Double confidence = normalizeConfidence(jsonObject.getDouble("confidence"));
        return buildParseResult(IntentEnum.getByCode(intentCode), entities, confidence);
    }

    /**
     * 解析实体数组。
     *
     * @param array 实体 JSON 数组
     * @return 实体列表
     */
    private List<EntityResult> parseEntityArray(JSONArray array) {
        List<EntityResult> results = new ArrayList<>();
        if (array == null) {
            return results;
        }

        for (int i = 0; i < array.size(); i++) {
            JSONObject obj = array.getJSONObject(i);
            if (obj == null) {
                log.warn("跳过无效实体项: {}", array.get(i));
                continue;
            }

            EntityTypeEnum type = EntityTypeEnum.getByCode(obj.getString("type"));
            String value = obj.getString("value");
            if (type == null || value == null || value.trim().isEmpty()) {
                log.warn("跳过非法实体项: {}", obj);
                continue;
            }

            results.add(EntityResult.builder()
                    .type(type)
                    .value(value.trim())
                    .build());
        }
        return results;
    }

    /**
     * 构建统一解析结果。
     *
     * @param intent 意图
     * @param entities 实体列表
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
     * 规范化意图编码。
     *
     * @param rawResult 模型原始返回
     * @return 规范化后的意图编码
     */
    private String normalizeIntentCode(String rawResult) {
        if (rawResult == null || rawResult.trim().isEmpty()) {
            return null;
        }

        String cleaned = sanitizeModelOutput(rawResult).toUpperCase(Locale.ROOT);
        for (IntentEnum intent : IntentEnum.values()) {
            if (cleaned.contains(intent.getCode())) {
                return intent.getCode();
            }
        }
        return null;
    }

    /**
     * 校验意图编码是否合法。
     *
     * @param intentCode 意图编码
     * @return 是否合法
     */
    private boolean isValidIntentCode(String intentCode) {
        if (intentCode == null || intentCode.trim().isEmpty()) {
            return false;
        }

        for (IntentEnum intent : IntentEnum.values()) {
            if (intent.getCode().equals(intentCode)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 规范化置信度。
     *
     * @param confidence 原始置信度
     * @return 合法的置信度
     */
    private Double normalizeConfidence(Double confidence) {
        if (confidence == null || confidence < 0.0D || confidence > 1.0D) {
            return DEFAULT_CONFIDENCE;
        }
        return confidence;
    }

    /**
     * 从模型输出中提取 JSON 片段。
     *
     * @param rawResult 模型原始返回
     * @param startChar JSON 起始字符
     * @param endChar JSON 结束字符
     * @return 提取后的 JSON 片段
     */
    private String extractJsonSegment(String rawResult, char startChar, char endChar) {
        String cleaned = sanitizeModelOutput(rawResult);
        int startIndex = cleaned.indexOf(startChar);
        int endIndex = cleaned.lastIndexOf(endChar);
        if (startIndex < 0 || endIndex < startIndex) {
            throw new IllegalArgumentException("未找到有效JSON片段: " + rawResult);
        }
        return cleaned.substring(startIndex, endIndex + 1);
    }

    /**
     * 清洗模型原始输出。
     *
     * @param rawResult 模型原始返回
     * @return 清洗后的结果
     */
    private String sanitizeModelOutput(String rawResult) {
        if (rawResult == null) {
            return "";
        }

        String cleaned = rawResult.trim();
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.replaceAll("^```(?:json)?\\s*", "");
            cleaned = cleaned.replaceAll("\\s*```$", "");
        }
        return cleaned.trim();
    }
}
