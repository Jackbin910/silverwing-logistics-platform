package com.silverwing.admin.trigger.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 用户导入视图对象（对应 RuoYi 的 SysUser 导入模板列）
 */
@Data
public class UserImportVO {

    @ExcelProperty("登录名称")
    private String username;

    @ExcelProperty("用户昵称")
    private String nickname;

    @ExcelProperty("性别")
    private Integer sex;

    @ExcelProperty("手机号码")
    private String phone;

    @ExcelProperty("邮箱")
    private String email;

    @ExcelProperty("状态")
    private Integer status;

    @ExcelProperty("部门ID")
    private Long deptId;
}
