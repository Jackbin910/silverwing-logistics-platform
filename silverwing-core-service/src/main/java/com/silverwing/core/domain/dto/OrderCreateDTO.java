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
    @NotBlank(message = "{validation.order.ordertype.notblank}")
    private String orderType;
    
    /**
     * 部门/科室
     */
    @NotBlank(message = "{validation.order.department.notblank}")
    private String department;
    
    /**
     * 目标位置（如：手术室1号）
     */
    @NotBlank(message = "{validation.order.targetlocation.notblank}")
    private String targetLocation;
    
    /**
     * 联系人
     */
    @NotBlank(message = "{validation.order.contactname.notblank}")
    private String contactName;
    
    /**
     * 联系电话
     */
    @NotBlank(message = "{validation.order.contactphone.notblank}")
    private String contactPhone;
    
    /**
     * 物品列表
     */
    @NotNull(message = "{validation.order.items.notnull}")
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
        @NotBlank(message = "{validation.order.item.itemcode.notblank}")
        private String itemCode;
        
        /**
         * 物品名称
         */
        @NotBlank(message = "{validation.order.item.itemname.notblank}")
        private String itemName;
        
        /**
         * 规格
         */
        private String specification;
        
        /**
         * 数量
         */
        @NotNull(message = "{validation.order.item.quantity.notnull}")
        private Integer quantity;
        
        /**
         * 单位
         */
        private String unit;
        
    }
    
}
