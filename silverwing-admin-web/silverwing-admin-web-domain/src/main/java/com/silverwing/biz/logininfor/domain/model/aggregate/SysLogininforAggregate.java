package com.silverwing.biz.logininfor.domain.model.aggregate;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 系统访问记录聚合根，对应数据库表 sys_logininfor。
 * <p>该表无审计字段与逻辑删除列，故为纯领域对象，仅承载业务属性。</p>
 */
@Data
public class SysLogininforAggregate implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 访问ID */
    private Long infoId;

    /** 用户账号 */
    private String userName;

    /** 登录IP地址 */
    private String ipaddr;

    /** 登录状态（0成功 1失败） */
    private String status;

    /** 提示信息 */
    private String msg;

    /** 访问时间 */
    private Date accessTime;
}
