package com.silverwing.admin.trigger.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统访问记录导出视图对象。
 *
 * @author silverwing
 */
@Data
public class LogininforExportVO {

    /** 访问ID */
    @ExcelProperty("访问编号")
    private Long infoId;

    /** 用户账号 */
    @ExcelProperty("用户账号")
    private String userName;

    /** 登录IP地址 */
    @ExcelProperty("登录IP")
    private String ipaddr;

    /** 登录状态（0成功 1失败） */
    @ExcelProperty("状态(0成功,1失败)")
    private Integer status;

    /** 提示信息 */
    @ExcelProperty("提示信息")
    private String msg;

    /** 访问时间 */
    @ExcelProperty("访问时间")
    private LocalDateTime accessTime;
}
