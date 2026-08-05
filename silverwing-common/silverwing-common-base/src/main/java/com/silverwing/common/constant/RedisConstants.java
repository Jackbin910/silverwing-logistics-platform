package com.silverwing.common.constant;

/**
 * Redis常量
 */
public class RedisConstants {
    
    /**
     * Token前缀
     */
    public static final String TOKEN_PREFIX = "silverwing:token:";
    
    /**
     * 用户信息前缀
     */
    public static final String USER_INFO_PREFIX = "silverwing:user:info:";
    
    /**
     * 用户权限前缀
     */
    public static final String USER_PERMISSION_PREFIX = "silverwing:user:permission:";
    
    /**
     * 设备状态前缀
     */
    public static final String DEVICE_STATUS_PREFIX = "silverwing:device:status:";
    
    /**
     * 订单信息前缀
     */
    public static final String ORDER_INFO_PREFIX = "silverwing:order:info:";
    
    /**
     * 库存信息前缀
     */
    public static final String INVENTORY_PREFIX = "silverwing:inventory:";
    
    /**
     * 实时定位前缀
     */
    public static final String LOCATION_PREFIX = "silverwing:location:";
    
    /**
     * 分布式锁前缀
     */
    public static final String LOCK_PREFIX = "silverwing:lock:";
    
    /**
     * MQ消息前缀
     */
    public static final String MQ_MESSAGE_PREFIX = "silverwing:mq:";

    /**
     * 登录密码错误次数缓存前缀（后接 username）
     */
    public static final String PWD_ERR_CNT_PREFIX = "silverwing:pwd:err:";

    /**
     * 密码最大错误次数（达到后锁定账号）
     */
    public static final int PASSWORD_MAX_RETRY_COUNT = 5;

    /**
     * 密码错误锁定时长（秒）
     */
    public static final long PASSWORD_LOCK_SECONDS = 600L;

}
