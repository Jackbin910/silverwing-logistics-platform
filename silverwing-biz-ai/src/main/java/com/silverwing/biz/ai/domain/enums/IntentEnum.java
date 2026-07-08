package com.silverwing.biz.ai.domain.enums;

import lombok.Getter;

/**
 * 意图枚举
 * 定义系统中支持的所有用户意图类型
 */
@Getter
public enum IntentEnum {

    QUERY_DEVICE_STATUS("QUERY_DEVICE_STATUS", "查询设备状态", "查询设备的在线状态、电量、运行模式等"),

    QUERY_DEVICE_LOCATION("QUERY_DEVICE_LOCATION", "查询设备位置", "查询设备当前所在的仓库、区域、坐标等"),

    QUERY_ORDER_STATUS("QUERY_ORDER_STATUS", "查询订单状态", "查询物流订单的当前状态、进度等"),

    QUERY_DEVICE_METRIC("QUERY_DEVICE_METRIC", "查询设备指标", "查询设备的温度、速度、电量等遥测指标"),

    CREATE_WORK_ORDER("CREATE_WORK_ORDER", "创建工单", "创建维修、巡检等工单"),

    MAINTENANCE_ASSIST("MAINTENANCE_ASSIST", "维修辅助", "请求设备维修指导和故障排查帮助"),

    FAULT_REPORT("FAULT_REPORT", "故障报告", "报告设备故障，自动创建维修工单"),

    KNOWLEDGE_QA("KNOWLEDGE_QA", "智能问答", "基于知识库的问答，如设备手册、操作规程"),

    DATA_STATISTICS("DATA_STATISTICS", "数据统计", "查询设备运行统计、效率分析等"),

    DATABASE_QUERY("DATABASE_QUERY", "数据库查询", "通过自然语言直接查询数据库，如订单列表、数据统计等"),

    OTHER("OTHER", "其他", "无法明确识别的意图");

    private final String code;
    private final String name;
    private final String description;

    IntentEnum(String code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }

    /**
     * 根据编码获取枚举
     *
     * @param code 意图编码
     * @return 对应的意图枚举，未找到返回 OTHER
     */
    public static IntentEnum getByCode(String code) {
        for (IntentEnum intent : values()) {
            if (intent.getCode().equals(code)) {
                return intent;
            }
        }
        return OTHER;
    }
}
