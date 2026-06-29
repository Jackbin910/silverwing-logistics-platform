package com.silverwing.common.interceptor;

import com.silverwing.common.constant.HeaderConstants;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

/**
 * Feign 链路追踪透传拦截器
 * <p>
 * 微服务间通过 OpenFeign 调用时，将当前线程 MDC 中的 traceId / spanId
 * 写入请求头透传给下游服务，确保跨服务调用链路不中断。
 * 下游服务通过 {@link com.silverwing.common.filter.RequestTraceFilter}
 * 从请求头读取 traceId 写入自身 MDC。
 * </p>
 * <p>
 * 注意：本拦截器仅在调用方线程上下文存在 traceId 时生效，
 * 异步线程调用 Feign 需自行透传 MDC 上下文。
 * </p>
 *
 * @author silverwing
 */
public class FeignTraceInterceptor implements RequestInterceptor {

    /**
     * MDC 中 traceId 的键名，与 RequestTraceFilter 保持一致
     */
    private static final String MDC_TRACE_ID = "traceId";

    /**
     * MDC 中 spanId 的键名，与 RequestTraceFilter 保持一致
     */
    private static final String MDC_SPAN_ID = "spanId";

    @Override
    public void apply(RequestTemplate template) {
        // 从当前线程 MDC 读取链路信息，透传至下游服务请求头
        String traceId = MDC.get(MDC_TRACE_ID);
        if (traceId != null && !traceId.isBlank()) {
            template.header(HeaderConstants.X_TRACE_ID, traceId);
        }
        String spanId = MDC.get(MDC_SPAN_ID);
        if (spanId != null && !spanId.isBlank()) {
            template.header(HeaderConstants.X_SPAN_ID, spanId);
        }
    }
}
