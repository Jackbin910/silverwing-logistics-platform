package com.silverwing.ai.common.util;

import dev.langchain4j.model.chat.response.StreamingHandle;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicReference;

/**
 * 流式生成取消工具
 * <p>SSE 客户端断开或超时会触发 Flux 取消，此处统一关闭底层模型连接，
 * 避免模型继续生成剩余 token 白白占用 GPU 与连接资源。</p>
 */
@Slf4j
public final class StreamingCancellation {

    /**
     * 工具类不允许实例化
     */
    private StreamingCancellation() {
    }

    /**
     * 取消底层模型的流式生成
     * <p>部分模型实现不支持取消（抛 UnsupportedFeatureException），降级为仅记录日志，
     * 不影响客户端断开流程。</p>
     *
     * @param handleRef 流式句柄引用，首个 token 到达前为 null
     * @param scene     业务场景，用于日志定位
     */
    public static void cancel(AtomicReference<StreamingHandle> handleRef, String scene) {
        StreamingHandle handle = handleRef.get();
        if (handle == null || handle.isCancelled()) {
            return;
        }
        try {
            handle.cancel();
            log.info("SSE 客户端断开，已取消底层模型流式生成: scene={}", scene);
        } catch (Exception e) {
            // 模型实现不支持取消时抛 UnsupportedFeatureException，仅记录警告，不中断断开流程
            log.warn("取消流式生成失败，模型可能不支持取消: scene={}, reason={}", scene, e.getMessage());
        }
    }
}
