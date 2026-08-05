package com.silverwing.biz.logininfor.domain.model.aggregate;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统访问记录聚合根。
 * <p>承载登录/访问日志的领域模型，作为应用层与仓储层之间流转的核心对象。</p>
 *
 * @author silverwing
 */
@Data
public class SysLogininforAggregate implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 访问ID
     */
    private Long infoId;

    /**
     * 用户账号
     */
    private String userName;

    /**
     * 登录IP地址
     */
    private String ipaddr;

    /**
     * 登录状态（0成功 1失败）
     */
    private Integer status;

    /**
     * 提示信息
     */
    private String msg;

    /**
     * 访问时间
     */
    private LocalDateTime accessTime;
}
