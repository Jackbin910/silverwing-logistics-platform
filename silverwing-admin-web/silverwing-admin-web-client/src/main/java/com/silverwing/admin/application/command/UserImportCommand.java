package com.silverwing.admin.application.command;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户导入命令（由 Excel 解析后传入防腐层）
 */
@Data
public class UserImportCommand implements Serializable {

    /** 登录名称 */
    private String username;

    /** 用户昵称 */
    private String nickname;

    /** 性别 */
    private Integer sex;

    /** 手机号码 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 状态 */
    private Integer status;

    /** 部门ID */
    private Long deptId;

    /** 用户类型（00系统用户） */
    private String userType;
}
