package com.silverwing.admin.application.command;

import lombok.Data;

/**
 * 更新用户命令
 */
@Data
public class UpdateUserCommand {

    private String avatar;
    private String phone;
    private String email;
    private Integer sex;
    private Integer status;

    /** 昵称 */
    private String nickname;

    /** 部门ID */
    private Long deptId;

    /** 用户类型（00系统用户） */
    private String userType;
}
