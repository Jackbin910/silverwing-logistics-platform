package com.silverwing.core.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单追踪DTO
 */
@Data
public class OrderTrackDTO {
    
    /**
     * 订单ID
     */
    private Long orderId;
    
    /**
     * 订单状态
     */
    private String status;
    
    /**
     * 订单状态描述
     */
    private String statusDesc;
    
    /**
     * 配送设备类型
     */
    private String deliveryType;
    
    /**
     * 配送设备ID
     */
    private String deviceId;
    
    /**
     * 当前位置
     */
    private LocationInfo currentLocation;
    
    /**
     * 目标位置
     */
    private String targetLocation;
    
    /**
     * 预计到达时间
     */
    private LocalDateTime estimatedArrivalTime;
    
    /**
     * 追踪记录
     */
    private List<TrackRecord> trackRecords;
    
    /**
     * 位置信息
     */
    @Data
    public static class LocationInfo {
        
        /**
         * 建筑物
         */
        private String building;
        
        /**
         * 楼层
         */
        private Integer floor;
        
        /**
         * 区域
         */
        private String area;
        
        /**
         * 经度
         */
        private Double longitude;
        
        /**
         * 纬度
         */
        private Double latitude;
        
    }
    
    /**
     * 追踪记录
     */
    @Data
    public static class TrackRecord {
        
        /**
         * 时间
         */
        private LocalDateTime time;
        
        /**
         * 事件
         */
        private String event;
        
        /**
         * 位置
         */
        private String location;
        
        /**
         * 描述
         */
        private String description;
        
    }
    
}
