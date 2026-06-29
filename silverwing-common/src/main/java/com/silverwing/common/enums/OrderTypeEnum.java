package com.silverwing.common.enums;

import lombok.Getter;

/**
 * 订单类型枚举
 */
@Getter
public enum OrderTypeEnum {
    
    /**
     * 手术物资配送
     */
    SURGERY_MATERIAL(1, "手术物资配送", "surgery_material"),
    
    /**
     * 批量物资配送
     */
    BULK_MATERIAL(2, "批量物资配送", "bulk_material"),
    
    /**
     * 药品配送
     */
    MEDICINE(3, "药品配送", "medicine"),
    
    /**
     * 样本配送
     */
    SAMPLE(4, "样本配送", "sample"),
    
    /**
     * 其他
     */
    OTHER(99, "其他", "other");
    
    /**
     * 类型编码
     */
    private final Integer code;
    
    /**
     * 类型名称
     */
    private final String name;
    
    /**
     * 类型标识
     */
    private final String type;
    
    OrderTypeEnum(Integer code, String name, String type) {
        this.code = code;
        this.name = name;
        this.type = type;
    }
    
    /**
     * 根据类型编码获取枚举
     */
    public static OrderTypeEnum getByCode(Integer code) {
        for (OrderTypeEnum orderType : values()) {
            if (orderType.getCode().equals(code)) {
                return orderType;
            }
        }
        return null;
    }
    
    /**
     * 根据类型标识获取枚举
     */
    public static OrderTypeEnum getByType(String type) {
        for (OrderTypeEnum orderType : values()) {
            if (orderType.getType().equals(type)) {
                return orderType;
            }
        }
        return null;
    }
    
}
