package com.silverwing.auth.iam.infrastructure.dao.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统访问记录持久化对象，对应 {@code sys_logininfor} 表。
 * <p>该表仅承载登录/访问日志写入，无审计字段，故不继承 BaseEntity。</p>
 *
 * @author silverwing
 */
@Data
@TableName("sys_logininfor")
public class SysLogininforPO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 访问ID
     */
    @TableId(type = IdType.AUTO)
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
