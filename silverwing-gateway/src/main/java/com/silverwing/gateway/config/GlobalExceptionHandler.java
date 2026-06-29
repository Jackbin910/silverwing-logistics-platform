package com.silverwing.gateway.config;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器（Gateway 层 - 响应式）
 * <p>
 * 统一拦截 Gateway 层抛出的异常，返回标准 Result 格式。
 * 注意：各微服务的异常由 common 模块的 GlobalExceptionHandler 处理。
 * </p>
 */
@Slf4j
@Component
@Order(-1)
public class GlobalExceptionHandler implements WebExceptionHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        
        // 构建统一的 Result 格式
        Map<String, Object> result = buildErrorResponse(ex);
        
        // 设置响应状态码
        Integer code = (Integer) result.get("code");
        response.setStatusCode(HttpStatus.valueOf(code));
        
        // 设置响应头
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(result);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            log.error("JSON序列化失败", e);
            return response.setComplete();
        }
    }

    /**
     * 构建错误响应
     */
    private Map<String, Object> buildErrorResponse(Throwable ex) {
        Map<String, Object> result = new HashMap<>();
        
        // 默认系统错误
        int code = 500;
        String message = "系统繁忙，请稍后重试";
        
        if (ex instanceof NotLoginException) {
            // 未登录异常
            log.warn("网关认证异常：{}", ex.getMessage());
            code = 401;
            message = "登录已过期，请重新登录";
        } else if (ex instanceof NotPermissionException) {
            // 权限不足异常
            log.warn("网关权限不足：{}", ex.getMessage());
            code = 403;
            message = "无操作权限";
        } else if (ex instanceof NotRoleException) {
            // 角色不足异常
            log.warn("网关角色不足：{}", ex.getMessage());
            code = 403;
            message = "无操作权限";
        } else if (ex instanceof ResponseStatusException) {
            // 响应状态异常
            ResponseStatusException rse = (ResponseStatusException) ex;
            code = rse.getStatusCode() != null ? rse.getStatusCode().value() : 500;
            message = rse.getReason() != null ? rse.getReason() : "请求错误";
            log.warn("网关响应异常 [code={}]：{}", code, message);
        } else {
            // 其他异常
            log.error("网关系统异常：", ex);
        }
        
        result.put("code", code);
        result.put("message", message);
        result.put("data", null);
        result.put("timestamp", System.currentTimeMillis());
        
        return result;
    }
}
