package com.silverwing.biz.logininfor.domain.model.query;

import com.silverwing.common.domain.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统访问记录查询条件（领域层）。
 * <p>继承分页请求基类，封装账号、IP、状态与访问时间区间过滤条件。</p>
 *
 * @author silverwing
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LogininforQuery extends PageRequest {

    /**
     * 用户账号（模糊匹配）
     */
    private String userName;

    /**
     * 登录IP地址（模糊匹配）
     */
    private String ipaddr;

    /**
     * 登录状态（0成功 1失败）
     */
    private Integer status;

    /**
     * 访问开始时间（格式 yyyy-MM-dd HH:mm:ss）
     */
    private String beginTime;

    /**
     * 访问结束时间（格式 yyyy-MM-dd HH:mm:ss）
     */
    private String endTime;
}
