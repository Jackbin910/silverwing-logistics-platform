package com.silverwing.twin.domain.vo;

import lombok.Data;

import java.util.Map;

/**
 * 驾驶舱概览VO
 */
@Data
public class DashboardOverviewVO {
    
    /**
     * 当前在途任务数
     */
    private Integer inTransitTasks;
    
    /**
     * 设备利用率
     */
    private Double deviceUtilization;
    
    /**
     * 今日配送次数
     */
    private Integer todayDeliveryCount;
    
    /**
     * 平均响应时间(分钟)
     */
    private Double avgResponseTime;
    
    /**
     * 库存水位
     */
    private Map<String, Object> inventoryLevel;
    
    /**
     * 故障设备数
     */
    private Integer faultDeviceCount;
    
    /**
     * 维护中设备数
     */
    private Integer maintenanceDeviceCount;
    
    /**
     * 本月传输量
     */
    private Integer monthlyTransmissionCount;
    
}
