package com.silverwing.core.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 路径规划响应DTO
 */
@Data
public class PathPlanResponse {
    
    /**
     * 推荐配送方式
     * 机器狗/机器人/AGV/人工
     */
    private String deliveryType;
    
    /**
     * 推荐配送设备ID
     */
    private String deviceId;
    
    /**
     * 推荐理由
     */
    private String reason;
    
    /**
     * 优化路径
     */
    private List<LocationNode> optimizedPath;
    
    /**
     * 预计配送时长(分钟)
     */
    private Integer estimatedDuration;
    
    /**
     * 预计到达时间
     */
    private LocalDateTime estimatedArrivalTime;
    
    /**
     * 仓储等待时间(分钟)
     */
    private Integer warehouseWaitTime;
    
    /**
     * 路径节点
     */
    @Data
    public static class LocationNode {
        
        /**
         * 序号
         */
        private Integer sequence;
        
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
         * 预计到达时间
         */
        private LocalDateTime estimatedArrival;
        
    }
    
}
