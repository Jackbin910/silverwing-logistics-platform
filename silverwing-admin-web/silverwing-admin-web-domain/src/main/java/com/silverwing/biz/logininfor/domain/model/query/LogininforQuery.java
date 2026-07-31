package com.silverwing.biz.logininfor.domain.model.query;

import com.silverwing.common.domain.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统访问记录查询条件（领域层）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LogininforQuery extends PageRequest {

    /** 用户账号（模糊） */
    private String userName;

    /** 登录IP地址（模糊） */
    private String ipaddr;

    /** 登录状态（0-成功 1-失败） */
    private String status;

    /** 开始时间（按访问时间 accessTime 查询） */
    private String beginTime;

    /** 结束时间（按访问时间 accessTime 查询） */
    private String endTime;
}
