package com.silverwing.ai.application.impl;

import com.silverwing.ai.application.dto.ClassifyResult;
import com.silverwing.biz.ai.domain.enums.TextCategoryEnum;
import com.silverwing.ai.application.ai.TextClassifier;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 文本分类服务实现
 */
@Slf4j
@Service
public class TextClassificationService {

    private final TextClassifier textClassifier;

    /**
     * 构造函数
     *
     * @param chatModel LangChain4j 聊天模型
     */
    public TextClassificationService(ChatModel chatModel) {
        this.textClassifier = AiServices.create(TextClassifier.class, chatModel);
    }

    /**
     * 对文本进行分类
     *
     * @param text 待分类的文本
     * @return 分类结果
     */
    public ClassifyResult classify(String text) {
        try {
            String categoryCode = textClassifier.classify(text);
            log.info("文本分类结果: {} <- \"{}\"", categoryCode, text.substring(0, Math.min(50, text.length())));
            TextCategoryEnum category = TextCategoryEnum.getByCode(categoryCode);
            return ClassifyResult.builder()
                    .category(category)
                    .confidence(0.8)
                    .description(category.getDescription())
                    .build();
        } catch (Exception e) {
            log.error("文本分类失败", e);
            return ClassifyResult.builder()
                    .category(TextCategoryEnum.OTHER)
                    .confidence(0.0)
                    .description("分类失败")
                    .build();
        }
    }
}
