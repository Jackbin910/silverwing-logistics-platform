package com.silverwing.biz.ai.domain.enums;

import lombok.Getter;

/**
 * 文本分类枚举
 * 定义工单、告警等文本的分类类型
 */
@Getter
public enum TextCategoryEnum {

    MECHANICAL_FAULT("MECHANICAL_FAULT", "机械故障", "齿轮、履带、传动机构等机械部件故障"),

    ELECTRICAL_FAULT("ELECTRICAL_FAULT", "电气故障", "电机、电池、电路板等电气部件故障"),

    SOFTWARE_FAULT("SOFTWARE_FAULT", "软件故障", "系统崩溃、程序错误、通信异常等软件问题"),

    SENSOR_ANOMALY("SENSOR_ANOMALY", "传感器异常", "激光雷达、摄像头、红外传感器等异常"),

    NETWORK_FAULT("NETWORK_FAULT", "网络通信故障", "WiFi、5G、蓝牙等通信模块问题"),

    ROUTINE_INSPECTION("ROUTINE_INSPECTION", "日常巡检", "例行检查、保养维护"),

    URGENT_ALARM("URGENT_ALARM", "紧急告警", "需要立即处理的紧急情况"),

    USER_FEEDBACK("USER_FEEDBACK", "用户反馈", "用户使用反馈、投诉、建议"),

    KNOWLEDGE_DOC("KNOWLEDGE_DOC", "知识文档", "设备手册、操作规程、技术文档"),

    OTHER("OTHER", "其他", "无法归类的文本");

    private final String code;
    private final String name;
    private final String description;

    TextCategoryEnum(String code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }

    /**
     * 根据编码获取枚举
     *
     * @param code 分类编码
     * @return 对应的分类枚举，未找到返回 OTHER
     */
    public static TextCategoryEnum getByCode(String code) {
        for (TextCategoryEnum category : values()) {
            if (category.getCode().equals(code)) {
                return category;
            }
        }
        return OTHER;
    }
}
