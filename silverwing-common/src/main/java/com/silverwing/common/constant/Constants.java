package com.silverwing.common.constant;

/**
 * 系统通用常量
 * <p>
 * 仅保留真正跨模块复用的通用常量。领域相关常量请使用对应的枚举类：
 * <ul>
 *   <li>设备类型：{@link com.silverwing.common.enums.DeviceTypeEnum}</li>
 *   <li>订单类型：{@link com.silverwing.common.enums.OrderTypeEnum}</li>
 * </ul>
 * </p>
 *
 * @author silverwing
 */
public final class Constants {

    /**
     * UTF-8 编码
     */
    public static final String UTF8 = "UTF-8";

    /**
     * 默认当前页
     */
    public static final int DEFAULT_CURRENT_PAGE = 1;

    /**
     * 默认每页条数
     */
    public static final int DEFAULT_PAGE_SIZE = 10;

    /**
     * Redis 缓存时间 - 1 小时（秒）
     */
    public static final long CACHE_ONE_HOUR = 60 * 60;

    /**
     * Redis 缓存时间 - 24 小时（秒）
     */
    public static final long CACHE_ONE_DAY = 24 * 60 * 60;

    /**
     * Redis 缓存时间 - 7 天（秒）
     */
    public static final long CACHE_ONE_WEEK = 7 * 24 * 60 * 60;

    private Constants() {
    }
}
