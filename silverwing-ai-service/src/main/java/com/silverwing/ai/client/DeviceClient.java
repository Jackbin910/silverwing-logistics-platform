package com.silverwing.ai.client;

import com.silverwing.common.domain.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * 设备服务 Feign 客户端
 * 调用 silverwing-core-service 查询设备信息
 */
@FeignClient(
    name = "silverwing-core-service",
    contextId = "deviceClient",
    path = "/api/device",
    fallbackFactory = DeviceClientFallback.class
)
public interface DeviceClient {

    /**
     * 根据设备编码查询设备详情
     *
     * @param code 设备编码
     * @return 设备信息
     */
    @GetMapping("/getByCode/{code}")
    Result<Map<String, Object>> getByCode(@PathVariable("code") String code);

    /**
     * 查询设备实时位置
     *
     * @param code 设备编码
     * @return 设备位置信息
     */
    @GetMapping("/location/{code}")
    Result<Map<String, Object>> getLocation(@PathVariable("code") String code);

    /**
     * 查询设备状态
     *
     * @param code 设备编码
     * @return 设备状态信息
     */
    @GetMapping("/status/{code}")
    Result<Map<String, Object>> getStatus(@PathVariable("code") String code);
}
