package com.silverwing.admin.application.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户响应DTO
 * <p>由 SysUserAggregate 经 UserConvertor 映射得到，作为触发层对外返回的用户视图；不含密码与盐值。</p>
 */
@Data
public class UserResponse implements Serializable {

    private Long id;

    private String username;

    private Integer sex;

    private String avatar;

    private String phone;

    private String email;

    private Integer status;

    /** 昵称 */
    private String nickname;

    /** 部门ID */
    private Long deptId;

    /** 用户类型（00系统用户） */
    private String userType;

    /** 最后登录IP */
    private String loginIp;

    /** 最后登录时间 */
    private LocalDateTime loginDate;

    /** 密码最新更新时间 */
    private LocalDateTime pwdUpdateDate;

    private String createBy;

    private String updateBy;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
