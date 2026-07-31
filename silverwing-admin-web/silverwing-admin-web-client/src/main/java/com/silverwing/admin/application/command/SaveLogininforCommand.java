package com.silverwing.admin.application.command;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 新增登录日志命令（内部调用，记录登录行为）。
 * <p>访问时间（access_time）由仓储层在落库时自动填充为当前时间，命令中无需传递。</p>
 */
@Data
public class SaveLogininforCommand implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 用户账号 */
    private String userName;

    /** 登录IP地址 */
    private String ipaddr;

    /** 登录状态（0-成功 1-失败） */
    private String status;

    /** 提示消息 */
    private String msg;
}
