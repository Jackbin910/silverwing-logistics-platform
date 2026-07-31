package com.silverwing.admin.application.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 登录日志响应对象（用于列表展示与 Excel 导出）。
 */
@Data
public class LogininforResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 访问ID */
    @ExcelProperty(value = "访问编号")
    private Long infoId;

    /** 用户账号 */
    @ExcelProperty(value = "用户账号")
    private String userName;

    /** 登录IP地址 */
    @ExcelProperty(value = "登录地址")
    private String ipaddr;

    /** 登录状态（0-成功 1-失败） */
    @ExcelProperty(value = "登录状态")
    private String status;

    /** 提示消息 */
    @ExcelProperty(value = "操作信息")
    private String msg;

    /** 访问时间 */
    @ExcelProperty(value = "访问时间")
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    private Date accessTime;
}
