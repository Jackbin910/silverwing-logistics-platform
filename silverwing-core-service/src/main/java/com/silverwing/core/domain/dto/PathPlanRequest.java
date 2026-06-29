package com.silverwing.core.domain.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 路径规划请求DTO
 */
@Data
public class PathPlanRequest {
    
    /**
     * 起始位置
     */
    @NotNull(message = "起始位置不能为空")
    private Location startLocation;
    
    /**
     * 目标位置列表
     */
    @NotEmpty(message = "目标位置不能为空")
    private List<Location> targetLocations;
    
    /**
     * 物品信息
     */
    @NotNull(message = "物品信息不能为空")
    private List<ItemInfo> items;
    
    /**
     * 是否紧急
     */
    private Boolean urgent = false;
    
    /**
     * 位置信息
     */
    @Data
    public static class Location {
        
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
        
    }
    
    /**
     * 物品信息
     */
    @Data
    public static class ItemInfo {
        
        /**
         * 物品类型
         */
        private String itemType;
        
        /**
         * 物品数量
         */
        private Integer quantity;
        
        /**
         * 物品重量(kg)
         */
        private Double weight;
        
        /**
         * 物品体积(m³)
         */
        private Double volume;
        
    }
    
}
