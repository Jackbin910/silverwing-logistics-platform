package com.silverwing.ai.client;

import com.silverwing.common.domain.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

/**
 * 设备服务 Feign 降级工厂
 * 当 core-service 不可用时提供默认数据
 */
@Slf4j
@Component
public class DeviceClientFallback implements FallbackFactory<DeviceClient> {

    @Override
    public DeviceClient create(Throwable cause) {
        log.error("设备服务调用失败，启用降级策略", cause);
        return new DeviceClient() {
            @Override
            public Result<Map<String, Object>> getByCode(String code) {
                log.warn("降级：使用模拟数据查询设备 {}", code);
                return Result.success(buildMockDevice(code));
            }

            @Override
            public Result<Map<String, Object>> getLocation(String code) {
                log.warn("降级：使用模拟数据查询设备位置 {}", code);
                return Result.success(buildMockLocation(code));
            }

            @Override
            public Result<Map<String, Object>> getStatus(String code) {
                log.warn("降级：使用模拟数据查询设备状态 {}", code);
                return Result.success(buildMockStatus(code));
            }
        };
    }

    private Map<String, Object> buildMockDevice(String code) {
        return Map.of(
            "deviceId", code,
            "deviceName", "AGV自动导引车",
            "deviceType", "AGV",
            "status", "在线",
            "batteryLevel", "85%",
            "warehouseName", "3号仓"
        );
    }

    private Map<String, Object> buildMockLocation(String code) {
        return Map.of(
            "deviceId", code,
            "warehouse", "3号仓",
            "area", "A区",
            "posX", 120.5,
            "posY", 45.2,
            "floor", "1F",
            "task", "正在配送药品至急诊科",
            "updateTime", "5秒前"
        );
    }

    private Map<String, Object> buildMockStatus(String code) {
        return Map.of(
            "deviceId", code,
            "status", "在线",
            "batteryLevel", "85%",
            "runMode", "自动模式",
            "runTime", "3小时20分",
            "lastHeartbeat", "10秒前"
        );
    }
}
