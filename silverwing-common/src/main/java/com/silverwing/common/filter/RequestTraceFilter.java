package com.silverwing.common.filter;

import com.silverwing.common.constant.HeaderConstants;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;

import java.io.IOException;
import java.util.UUID;

/**
 * 请求链路 ID 过滤器
 * <p>
 * 为每个 HTTP 请求生成唯一的 traceId 并写入 MDC，
 * 使 Logback 日志格式中的 %X{traceId} 能正确输出，
 * 即使未接入 Zipkin 等分布式追踪也能按请求追踪日志。
 * </p>
 * <p>
 * 优先从上游请求头（{@link HeaderConstants#X_TRACE_ID}）获取 traceId，
 * 适配网关转发与 Feign 跨服务调用透传场景。
 * </p>
 *
 * @author silverwing
 */
public class RequestTraceFilter implements Filter {

    /**
     * MDC 中 traceId 的键名，与 logback-spring.xml 中 %X{traceId} 对应
     */
    private static final String MDC_TRACE_ID = "traceId";

    /**
     * MDC 中 spanId 的键名，与 logback-spring.xml 中 %X{spanId} 对应
     */
    private static final String MDC_SPAN_ID = "spanId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        // 优先从上游请求头获取 traceId（网关/Feign 转发场景），否则生成新的
        String traceId = httpRequest.getHeader(HeaderConstants.X_TRACE_ID);
        if (traceId == null || traceId.isBlank()) {
            traceId = generateTraceId();
        }

        // spanId 使用请求 UUID 的前 8 位
        String spanId = UUID.randomUUID().toString().replace("-", "").substring(0, 8);

        try {
            MDC.put(MDC_TRACE_ID, traceId);
            MDC.put(MDC_SPAN_ID, spanId);

            // 将 traceId 写入响应头，方便前端/调用方排查问题
            if (response instanceof HttpServletResponse httpResponse) {
                httpResponse.setHeader(HeaderConstants.X_TRACE_ID, traceId);
            }

            chain.doFilter(request, response);
        } finally {
            // 请求结束后清理 MDC，防止线程池复用导致 traceId 串线程
            MDC.clear();
        }
    }

    /**
     * 生成短格式 traceId（32 位十六进制）
     */
    private String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
