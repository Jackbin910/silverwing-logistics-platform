package com.silverwing.core.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 订单创建DTO
 */
@Data
public class OrderCreateDTO {
    
    /**
     * 订单类型：手术物资、批量物资、药品、样本
     */
    @NotBlank(message = "订单类型不能为空")
    private String orderType;
    
    /**
     * 部门/科室
     */
    @NotBlank(message = "部门不能为空")
    private String department;
    
    /**
     * 目标位置（如：手术室1号）
     */
    @NotBlank(message = "目标位置不能为空")
    private String targetLocation;
    
    /**
     * 联系人
     */
    @NotBlank(message = "联系人不能为空")
    private String contactName;
    
    /**
     * 联系电话
     */
    @NotBlank(message = "联系电话不能为空")
    private String contactPhone;
    
    /**
     * 物品列表
     */
    @NotNull(message = "物品列表不能为空")
    private List<OrderItemDTO> items;
    
    /**
     * 是否紧急
     */
    private Boolean urgent = false;
    
    /**
     * 备注信息
     */
    private String remark;
    
    /**
     * 订单项DTO
     */
    @Data
    public static class OrderItemDTO {
        
        /**
         * 物品编码
         */
        @NotBlank(message = "物品编码不能为空")
        private String itemCode;
        
        /**
         * 物品名称
         */
        @NotBlank(message = "物品名称不能为空")
        private String itemName;
        
        /**
         * 规格
         */
        private String specification;
        
        /**
         * 数量
         */
        @NotNull(message = "数量不能为空")
        private Integer quantity;
        
        /**
         * 单位
         */
        private String unit;
        
    }
    
}
