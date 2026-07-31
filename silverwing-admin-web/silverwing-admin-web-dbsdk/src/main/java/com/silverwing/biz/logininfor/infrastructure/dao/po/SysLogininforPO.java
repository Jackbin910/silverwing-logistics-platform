package com.silverwing.biz.logininfor.infrastructure.dao.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 系统访问记录持久化对象（PO），对应 sys_logininfor 表。
 * <p>该表无审计字段（create_by/update_by 等）与逻辑删除列，故不继承 BaseEntity，
 * 仅映射真实表结构的 6 个列：info_id、user_name、ipaddr、status、msg、access_time。</p>
 */
@Data
@TableName(value = "sys_logininfor")
public class SysLogininforPO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 访问ID */
    @TableId(type = IdType.AUTO)
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
