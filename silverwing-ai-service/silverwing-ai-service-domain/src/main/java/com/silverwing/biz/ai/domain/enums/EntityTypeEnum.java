package com.silverwing.biz.ai.domain.enums;

import lombok.Getter;

/**
 * 实体类型枚举
 * 定义 NLP 可提取的命名实体类型
 */
@Getter
public enum EntityTypeEnum {

    DEVICE_ID("DEVICE_ID", "设备编码", "如 AGV-002、ROBOT-001"),

    WAREHOUSE("WAREHOUSE", "仓库", "如 3号仓、中心仓库"),

    AREA("AREA", "区域", "如 A区、B区、一楼"),

    ORDER_NO("ORDER_NO", "订单号", "如 ORD-20260330-001"),

    LOCATION("LOCATION", "位置", "具体的物理位置描述"),

    TIME("TIME", "时间", "如 今天、昨天、上周、具体日期"),

    PERSON("PERSON", "人员", "如 张三、维修工程师"),

    METRIC("METRIC", "指标名", "如 温度、电量、速度、振动值"),

    VALUE("VALUE", "指标值", "如 85%、42°C、1200小时"),

    FAULT_DESC("FAULT_DESC", "故障描述", "设备故障的现象描述"),

    WORK_ORDER_TYPE("WORK_ORDER_TYPE", "工单类型", "如 维修、巡检、保养"),

    DEVICE_TYPE("DEVICE_TYPE", "设备类型", "如 AGV、机器狗、配送机器人");

    private final String code;
    private final String name;
    private final String description;

    EntityTypeEnum(String code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }

    /**
     * 根据编码获取枚举
     *
     * @param code 类型编码
     * @return 对应的实体类型枚举，未找到返回 null
     */
    public static EntityTypeEnum getByCode(String code) {
        for (EntityTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
