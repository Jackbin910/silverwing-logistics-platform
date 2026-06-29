package com.silverwing.twin.domain.vo;

import lombok.Data;

import java.util.List;

/**
 * 设备树VO
 */
@Data
public class DeviceTreeVO {
    
    /**
     * 设备树节点列表
     */
    private List<TreeNode> nodes;
    
    /**
     * 树节点
     */
    @Data
    public static class TreeNode {
        
        /**
         * 节点ID
         */
        private String id;
        
        /**
         * 节点名称
         */
        private String name;
        
        /**
         * 节点类型: building-建筑, floor-楼层, area-区域, device-设备
         */
        private String type;
        
        /**
         * 设备类型
         */
        private String deviceType;
        
        /**
         * 状态
         */
        private String status;
        
        /**
         * 子节点
         */
        private List<TreeNode> children;
        
    }
    
}
