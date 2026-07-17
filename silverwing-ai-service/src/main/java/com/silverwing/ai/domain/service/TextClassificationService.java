package com.silverwing.ai.domain.service;

import com.silverwing.ai.domain.model.ClassifyResult;
import com.silverwing.ai.domain.port.LlmPort;
import com.silverwing.biz.ai.domain.enums.TextCategoryEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 文本分类服务实现
 */
@Slf4j
@Service
public class TextClassificationService {

    private static final String SYSTEM_PROMPT = """
        你是物流智能平台的文本分类引擎。对以下文本进行分类。

        可选分类（只返回分类编码，不要返回其他内容）：
        - MECHANICAL_FAULT：机械故障（齿轮、履带、传动机构等）
        - ELECTRICAL_FAULT：电气故障（电机、电池、电路板等）
        - SOFTWARE_FAULT：软件故障（系统崩溃、程序错误、通信异常）
        - SENSOR_ANOMALY：传感器异常（激光雷达、摄像头、红外传感器）
        - NETWORK_FAULT：网络通信故障（WiFi、5G、蓝牙）
        - ROUTINE_INSPECTION：日常巡检（例行检查、保养维护）
        - URGENT_ALARM：紧急告警（需要立即处理的紧急情况）
        - USER_FEEDBACK：用户反馈（投诉、建议）
        - KNOWLEDGE_DOC：知识文档（设备手册、操作规程）
        - OTHER：其他

        规则：
        1. 只返回分类编码字符串
        2. 根据文本内容判断最可能的分类
        """;

    private final LlmPort llmPort;

    /**
     * 构造函数
     *
     * @param llmPort LLM 调用端口
     */
    public TextClassificationService(LlmPort llmPort) {
        this.llmPort = llmPort;
    }

    /**
     * 对文本进行分类
     *
     * @param text 待分类的文本
     * @return 分类结果
     */
    public ClassifyResult classify(String text) {
        try {
            String categoryCode = llmPort.complete(SYSTEM_PROMPT, text);
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
