package com.silverwing.twin.controller;

import com.silverwing.common.domain.Result;
import com.silverwing.twin.domain.vo.RealtimeDeviceStatusVO;
import com.silverwing.twin.service.VisualizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 可视化控制器
 * 3D数字孪生模型展示、实时数据推送
 */
@Tag(name = "数字孪生可视化", description = "3D模型、实时数据推送等接口")
@RestController
@RequestMapping("/visualization")
public class VisualizationController {
    
    @Resource
    private VisualizationService visualizationService;
    
    /**
     * 3D模型配置
     */
    @Operation(summary = "获取3D模型配置")
    @GetMapping("/model/config")
    public Result<?> getModelConfig(@RequestParam(value = "scene", required = false) String scene) {
        return Result.success(visualizationService.getModelConfig(scene));
    }
    
    /**
     * 设备实时状态
     */
    @Operation(summary = "设备实时状态")
    @GetMapping("/device/status")
    public Result<RealtimeDeviceStatusVO> getRealtimeDeviceStatus() {
        RealtimeDeviceStatusVO status = visualizationService.getRealtimeDeviceStatus();
        return Result.success(status);
    }
    
    /**
     * 设备详细数据
     */
    @Operation(summary = "设备详细数据")
    @GetMapping("/device/detail/{deviceId}")
    public Result<?> getDeviceDetail(@PathVariable String deviceId) {
        return Result.success(visualizationService.getDeviceDetail(deviceId));
    }
    
    /**
     * 场景模拟推演
     * 新建科室对物流系统的影响评估、高峰期应急预案模拟等
     */
    @Operation(summary = "场景模拟")
    @PostMapping("/simulation")
    public Result<Map<String, Object>> runSimulation(@RequestBody Map<String, Object> params) {
        return Result.success(visualizationService.runSimulation(params));
    }
    
}
