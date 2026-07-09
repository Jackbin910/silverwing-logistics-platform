package com.silverwing.ai.application;

import com.silverwing.ai.application.dto.*;

import java.util.List;

/**
 * NLP 自然语言处理统一服务接口
 * 提供意图识别、实体提取、文本分类、情感分析、文本摘要等能力
 */
public interface NlpService {

    /**
     * 意图识别
     *
     * @param text 用户自然语言输入
     * @return 意图识别结果
     */
    IntentResult recognizeIntent(String text);

    /**
     * 命名实体提取
     *
     * @param text 用户自然语言输入
     * @return 实体列表
     */
    List<EntityResult> extractEntities(String text);

    /**
     * 文本分类
     *
     * @param text 待分类的文本
     * @return 分类结果
     */
    ClassifyResult classifyText(String text);

    /**
     * 情感分析
     *
     * @param text 待分析的文本
     * @return 情感分析结果
     */
    SentimentResult analyzeSentiment(String text);

    /**
     * 文本摘要
     *
     * @param text 待摘要的文本
     * @return 摘要结果
     */
    SummaryResult summarizeText(String text);

    /**
     * 综合分析（意图 + 实体 + 分类 + 情感）
     *
     * @param text 用户自然语言输入
     * @return 综合分析结果
     */
    NlpAnalyzeResult analyze(String text);
}
