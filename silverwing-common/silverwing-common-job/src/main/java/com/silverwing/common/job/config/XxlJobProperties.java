package com.silverwing.common.job.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * XXL-Job 执行器配置属性，对应配置文件中的 {@code xxl.job} 前缀。
 *
 * @author silverwing
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "xxl.job")
public class XxlJobProperties {

    /** 调度中心地址，多个以逗号分隔 */
    private String adminAddresses;

    /** 执行器 AppName（集群分组依据） */
    private String appname;

    /** 执行器地址（选填，为空时自动获取） */
    private String address;

    /** 执行器 IP（选填，为空时自动获取） */
    private String ip;

    /** 执行器端口 */
    private int port = 9999;

    /** 执行器通讯令牌（与调度中心一致） */
    private String accessToken;

    /** 执行器日志路径 */
    private String logPath = "/data/applogs/xxl-job/jobhandler";

    /** 执行器日志保留天数 */
    private int logRetentionDays = 30;
}
