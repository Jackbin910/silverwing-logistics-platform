package com.silverwing.twin.service;

import com.silverwing.twin.domain.vo.RealtimeDeviceStatusVO;

import java.util.Map;

/**
 * 可视化服务
 */
public interface VisualizationService {
    
    /**
     * 获取3D模型配置
     */
    Map<String, Object> getModelConfig(String scene);
    
    /**
     * 获取设备实时状态
     */
    RealtimeDeviceStatusVO getRealtimeDeviceStatus();
    
    /**
     * 获取设备详细数据
     */
    Map<String, Object> getDeviceDetail(String deviceId);
    
    /**
     * 运行场景模拟
     */
    Map<String, Object> runSimulation(Map<String, Object> params);
    
}
