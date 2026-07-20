package com.silverwing.twin.service;

import com.silverwing.twin.domain.vo.DashboardOverviewVO;
import com.silverwing.twin.domain.vo.DeviceTreeVO;

import java.util.List;
import java.util.Map;

/**
 * 驾驶舱服务
 */
public interface DashboardService {
    
    /**
     * 获取全院概览数据
     */
    DashboardOverviewVO getOverview();
    
    /**
     * 获取设备树结构
     */
    DeviceTreeVO getDeviceTree();
    
    /**
     * 获取物流压力热力图数据
     */
    List<Map<String, Object>> getHeatmapData();
    
    /**
     * 获取关键指标
     */
    Map<String, Object> getMetrics();
    
}
