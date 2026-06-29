package com.silverwing.twin.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.silverwing.common.domain.Result;
import com.silverwing.twin.domain.vo.DashboardOverviewVO;
import com.silverwing.twin.domain.vo.DeviceTreeVO;
import com.silverwing.twin.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 驾驶舱控制器
 * 场景四：院长驾驶舱与决策支持
 */
@Tag(name = "院长驾驶舱", description = "运营数据大屏、可视化展示等接口")
@RestController
@RequestMapping("/dashboard")
public class DashboardController {
    
    @Resource
    private DashboardService dashboardService;
    
    /**
     * 全院概览数据
     */
    @SaCheckPermission("dashboard:overview")
    @Operation(summary = "全院概览")
    @GetMapping("/overview")
    public Result<DashboardOverviewVO> getOverview() {
        DashboardOverviewVO overview = dashboardService.getOverview();
        return Result.success(overview);
    }
    
    /**
     * 设备树结构
     */
    @SaCheckPermission("dashboard:devicetree")
    @Operation(summary = "设备树")
    @GetMapping("/device-tree")
    public Result<DeviceTreeVO> getDeviceTree() {
        DeviceTreeVO deviceTree = dashboardService.getDeviceTree();
        return Result.success(deviceTree);
    }
    
    /**
     * 物流压力热力图数据
     */
    @SaCheckPermission("dashboard:heatmap")
    @Operation(summary = "物流压力热力图")
    @GetMapping("/heatmap")
    public Result<?> getHeatmapData() {
        return Result.success(dashboardService.getHeatmapData());
    }
    
    /**
     * 关键指标仪表盘
     */
    @Operation(summary = "关键指标")
    @GetMapping("/metrics")
    public Result<?> getMetrics() {
        return Result.success(dashboardService.getMetrics());
    }
    
}
