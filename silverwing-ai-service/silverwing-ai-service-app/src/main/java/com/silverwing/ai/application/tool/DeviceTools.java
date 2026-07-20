package com.silverwing.ai.application.tool;

import com.silverwing.ai.client.DeviceClient;
import com.silverwing.common.domain.Result;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 物流平台设备工具类
 * 通过 @Tool 注解将 Java 方法暴露给 LLM 调用
 * LLM 会根据用户意图自动选择合适的工具
 */
@Slf4j
@RequiredArgsConstructor
public class DeviceTools {

    private final DeviceClient deviceClient;

    @Tool("根据设备编码查询设备详细信息，包括设备名称、类型、状态、位置等")
    public String getDeviceInfo(
            @P("设备的唯一编码，例如 AGV-002、SC-001") String deviceCode) {
        try {
            Result<Map<String, Object>> result = deviceClient.getByCode(deviceCode);
            if (result != null && result.isSuccess() && result.getData() != null) {
                return formatDeviceInfo(result.getData());
            }
            return "未找到设备编码为【" + deviceCode + "】的设备信息";
        } catch (Exception e) {
            log.error("查询设备详情失败: deviceCode={}", deviceCode, e);
            return "查询设备详情失败: " + e.getMessage();
        }
    }

    /**
     * 查询设备实时位置
     */
    @Tool("查询物流设备的实时位置坐标和区域信息")
    public String getDeviceLocation(
            @P("设备的唯一编码") String deviceCode) {
        try {
            Result<Map<String, Object>> result = deviceClient.getLocation(deviceCode);
            if (result != null && result.isSuccess() && result.getData() != null) {
                Map<String, Object> data = result.getData();
                return String.format("设备【%s】当前位置: 区域=%s, X坐标=%s, Y坐标=%s, 更新时间=%s",
                        deviceCode,
                        data.getOrDefault("area", "未知"),
                        data.getOrDefault("x", "未知"),
                        data.getOrDefault("y", "未知"),
                        data.getOrDefault("updateTime", "未知"));
            }
            return "未找到设备【" + deviceCode + "】的位置信息";
        } catch (Exception e) {
            log.error("查询设备位置失败: deviceCode={}", deviceCode, e);
            return "查询设备位置失败: " + e.getMessage();
        }
    }

    @Tool("查询物流设备的运行状态，包括在线/离线、工作中/空闲、告警状态等")
    public String getDeviceStatus(
            @P("设备的唯一编码") String deviceCode) {
        try {
            Result<Map<String, Object>> result = deviceClient.getStatus(deviceCode);
            if (result != null && result.isSuccess() && result.getData() != null) {
                Map<String, Object> data = result.getData();
                return String.format("设备【%s】状态: 运行状态=%s, 健康状态=%s, 最后在线时间=%s",
                        deviceCode,
                        data.getOrDefault("runStatus", "未知"),
                        data.getOrDefault("healthStatus", "未知"),
                        data.getOrDefault("lastOnlineTime", "未知"));
            }
            return "未找到设备【" + deviceCode + "】的状态信息";
        } catch (Exception e) {
            log.error("查询设备状态失败: deviceCode={}", deviceCode, e);
            return "查询设备状态失败: " + e.getMessage();
        }
    }

    private String formatDeviceInfo(Map<String, Object> data) {
        return String.format(
                "设备信息:\n" +
                "  编码: %s\n" +
                "  名称: %s\n" +
                "  类型: %s\n" +
                "  区域: %s\n" +
                "  状态: %s\n" +
                "  创建时间: %s",
                data.getOrDefault("code", "未知"),
                data.getOrDefault("name", "未知"),
                data.getOrDefault("type", "未知"),
                data.getOrDefault("area", "未知"),
                data.getOrDefault("status", "未知"),
                data.getOrDefault("createTime", "未知")
        );
    }
}
