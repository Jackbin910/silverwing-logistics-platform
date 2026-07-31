package com.silverwing.admin.trigger.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户导出视图对象（对应 RuoYi 的 SysUser 导出列）
 */
@Data
public class UserExportVO {

    @ExcelProperty("用户ID")
    private Long id;

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

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;
}
