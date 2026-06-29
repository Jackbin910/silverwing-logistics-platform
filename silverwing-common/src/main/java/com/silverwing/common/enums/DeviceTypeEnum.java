package com.silverwing.common.enums;

import lombok.Getter;

/**
 * 设备类型枚举
 */
@Getter
public enum DeviceTypeEnum {
    
    /**
     * 机器狗
     */
    ROBOT_DOG(1, "机器狗", "robot_dog"),
    
    /**
     * 配送机器人
     */
    ROBOT(2, "配送机器人", "robot"),
    
    /**
     * AGV
     */
    AGV(3, "AGV", "agv"),
    
    /**
     * 气动物流系统
     */
    PNEUMATIC(4, "气动物流系统", "pneumatic"),
    
    /**
     * 传输站点
     */
    STATION(5, "传输站点", "station"),
    
    /**
     * 智能仓储设备
     */
    WAREHOUSE(6, "智能仓储设备", "warehouse"),
    
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
    
    DeviceTypeEnum(Integer code, String name, String type) {
        this.code = code;
        this.name = name;
        this.type = type;
    }
    
    /**
     * 根据类型编码获取枚举
     */
    public static DeviceTypeEnum getByCode(Integer code) {
        for (DeviceTypeEnum deviceType : values()) {
            if (deviceType.getCode().equals(code)) {
                return deviceType;
            }
        }
        return null;
    }
    
    /**
     * 根据类型标识获取枚举
     */
    public static DeviceTypeEnum getByType(String type) {
        for (DeviceTypeEnum deviceType : values()) {
            if (deviceType.getType().equals(type)) {
                return deviceType;
            }
        }
        return null;
    }
    
}
