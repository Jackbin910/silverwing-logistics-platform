package com.silverwing.ai.application.impl;

import com.silverwing.ai.application.dto.*;
import com.silverwing.ai.application.NlpService;
import com.silverwing.ai.domain.service.IntentRecognitionService;
import com.silverwing.ai.domain.service.SentimentAnalysisService;
import com.silverwing.ai.domain.service.TextClassificationService;
import com.silverwing.ai.domain.service.TextSummaryService;
import com.silverwing.biz.ai.domain.enums.IntentEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import com.silverwing.ai.domain.model.IntentResult;
import com.silverwing.ai.domain.model.NlpParseResult;
import com.silverwing.ai.domain.model.EntityResult;
import com.silverwing.ai.domain.model.ClassifyResult;
import com.silverwing.ai.domain.model.SentimentResult;
import com.silverwing.ai.domain.model.SummaryResult;
import com.silverwing.ai.domain.model.NlpAnalyzeResult;

/**
 * NLP 自然语言处理服务实现
 * 聚合各项 NLP 能力，提供统一的调用入口
 */
@Slf4j
@Service
public class NlpServiceImpl implements NlpService {

    private final IntentRecognitionService intentService;
    private final TextClassificationService classificationService;
    private final SentimentAnalysisService sentimentService;
    private final TextSummaryService summaryService;

    /**
     * 构造函数
     *
     * @param intentService        意图识别服务
     * @param classificationService 文本分类服务
     * @param sentimentService     情感分析服务
     * @param summaryService       文本摘要服务
     */
    public NlpServiceImpl(IntentRecognitionService intentService,
                          TextClassificationService classificationService,
                          SentimentAnalysisService sentimentService,
                          TextSummaryService summaryService) {
        this.intentService = intentService;
        this.classificationService = classificationService;
        this.sentimentService = sentimentService;
        this.summaryService = summaryService;
    }

    @Override
    public IntentResult recognizeIntent(String text) {
        try {
            return buildIntentResult(intentService.parseWithEntities(text));
        } catch (Exception e) {
            log.error("意图识别失败", e);
            return IntentResult.builder()
                    .intent(IntentEnum.OTHER)
                    .confidence(0.0)
                    .description("识别失败")
                    .build();
        }
    }

    @Override
    public List<EntityResult> extractEntities(String text) {
        return intentService.extractEntities(text);
    }

    @Override
    public ClassifyResult classifyText(String text) {
        return classificationService.classify(text);
    }

    @Override
    public SentimentResult analyzeSentiment(String text) {
        return sentimentService.analyze(text);
    }

    @Override
    public SummaryResult summarizeText(String text) {
        return summaryService.summarize(text);
    }

    @Override
    public NlpAnalyzeResult analyze(String text) {
        log.info("开始综合 NLP 分析, 文本长度: {}", text.length());

        // 意图与实体由同一次 LLM 调用产出，避免重复调用 parseWithEntities
        NlpParseResult parseResult = intentService.parseWithEntities(text);
        IntentResult intentResult = buildIntentResult(parseResult);

        return NlpAnalyzeResult.builder()
                .intentResult(intentResult)
                .entities(parseResult.getEntities())
                .classifyResult(classifyText(text))
                .sentimentResult(analyzeSentiment(text))
                .build();
    }

    /**
     * 将统一 NLP 解析结果转换为意图识别结果
     *
     * @param parseResult 统一解析结果（含意图、实体与置信度）
     * @return 意图识别结果
     */
    private IntentResult buildIntentResult(NlpParseResult parseResult) {
        IntentEnum intent = parseResult.getIntent();
        return IntentResult.builder()
                .intent(intent)
                .confidence(parseResult.getConfidence())
                .description(intent.getDescription())
                .build();
    }
}
