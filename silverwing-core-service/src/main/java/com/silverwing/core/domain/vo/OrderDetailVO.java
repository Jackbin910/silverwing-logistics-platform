package com.silverwing.core.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单详情VO
 */
@Data
public class OrderDetailVO {
    
    /**
     * 订单ID
     */
    private Long orderId;
    
    /**
     * 订单编号
     */
    private String orderNo;
    
    /**
     * 订单类型
     */
    private String orderType;
    
    /**
     * 订单状态
     */
    private String status;
    
    /**
     * 部门
     */
    private String department;
    
    /**
     * 目标位置
     */
    private String targetLocation;
    
    /**
     * 联系人
     */
    private String contactName;
    
    /**
     * 联系电话
     */
    private String contactPhone;
    
    /**
     * 物品列表
     */
    private List<OrderItemVO> items;
    
    /**
     * 配送方式
     */
    private String deliveryType;
    
    /**
     * 配送设备ID
     */
    private String deviceId;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 期望送达时间
     */
    private LocalDateTime expectedArrivalTime;
    
    /**
     * 实际送达时间
     */
    private LocalDateTime actualArrivalTime;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 订单项VO
     */
    @Data
    public static class OrderItemVO {
        
        /**
         * 物品编码
         */
        private String itemCode;
        
        /**
         * 物品名称
         */
        private String itemName;
        
        /**
         * 规格
         */
        private String specification;
        
        /**
         * 数量
         */
        private Integer quantity;
        
        /**
         * 单位
         */
        private String unit;
        
    }
    
}
