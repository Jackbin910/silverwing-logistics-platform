package com.silverwing.twin.domain.vo;

import lombok.Data;

import java.util.Map;

/**
 * 设备实时状态VO
 */
@Data
public class RealtimeDeviceStatusVO {
    
    /**
     * 设备总数
     */
    private Integer totalDevices;
    
    /**
     * 正常设备数
     */
    private Integer normalDevices;
    
    /**
     * 故障设备数
     */
    private Integer faultDevices;
    
    /**
     * 维护中设备数
     */
    private Integer maintenanceDevices;
    
    /**
     * 在线设备列表
     */
    private Map<String, Object> onlineDevices;
    
    /**
     * 离线设备列表
     */
    private Map<String, Object> offlineDevices;
    
}
