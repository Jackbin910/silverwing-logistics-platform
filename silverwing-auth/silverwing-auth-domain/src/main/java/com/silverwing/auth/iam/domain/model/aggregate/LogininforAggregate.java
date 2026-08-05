package com.silverwing.auth.iam.domain.model.aggregate;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统访问记录聚合根（领域层）。
 * <p>承载登录/访问日志的领域模型，由认证流程在登录成功或失败时写入。</p>
 *
 * @author silverwing
 */
@Data
public class LogininforAggregate implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

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
