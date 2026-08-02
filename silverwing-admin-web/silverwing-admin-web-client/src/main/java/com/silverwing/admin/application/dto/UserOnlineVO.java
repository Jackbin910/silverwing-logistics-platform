package com.silverwing.admin.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 在线用户视图对象
 * <p>从 Sa-Token 的登录会话中聚合展示，字段对齐 RuoYi 在线用户监控。</p>
 *
 * @author silverwing
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserOnlineVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 会话 token（强退时使用）
     */
    private String tokenId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 登录用户名
     */
    private String userName;

    /**
     * 登录 IP
     */
    private String ipaddr;

    /**
     * 登录地点（暂未采集，预留）
     */
    private String loginLocation;

    /**
     * 浏览器
     */
    private String browser;

    /**
     * 操作系统
     */
    private String os;

    /**
     * 登录时间
     */
    private LocalDateTime loginTime;
}
