package com.silverwing.common.operlog;

/**
 * 操作日志记录器
 * <p>
 * 由各微服务自行实现并注册为 Spring Bean，将 {@link OperLog} 持久化到本服务数据库。
 * 框架层（common）只负责采集，不关心存储位置；未注册任何实现时日志会被安全丢弃。
 * </p>
 */
public interface OperLogRecorder {

    /**
     * 记录一条操作日志
     *
     * @param operLog 操作日志
     */
    void record(OperLog operLog);
}
