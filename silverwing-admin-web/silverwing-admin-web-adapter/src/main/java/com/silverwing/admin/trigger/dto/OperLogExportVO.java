package com.silverwing.admin.trigger.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志导出视图对象。
 */
@Data
public class OperLogExportVO {

    /** 日志编号 */
    @ExcelProperty("日志编号")
    private Long operId;

    /** 系统模块 */
    @ExcelProperty("系统模块")
    private String title;

    /** 操作类型 */
    @ExcelProperty("操作类型")
    private Integer businessType;

    /** 操作人员 */
    @ExcelProperty("操作人员")
    private String operName;

    /** 部门名称 */
    @ExcelProperty("部门名称")
    private String deptName;

    /** 请求URL */
    @ExcelProperty("请求地址")
    private String operUrl;

    /** 主机地址 */
    @ExcelProperty("操作地址")
    private String operIp;

    /** 操作地点 */
    @ExcelProperty("操作地点")
    private String operLocation;

    /** 请求方式 */
    @ExcelProperty("请求方式")
    private String requestMethod;

    /** 操作方法 */
    @ExcelProperty("操作方法")
    private String method;

    /** 请求参数 */
    @ExcelProperty("请求参数")
    private String operParam;

    /** 返回参数 */
    @ExcelProperty("返回参数")
    private String jsonResult;

    /** 操作状态 */
    @ExcelProperty("状态")
    private Integer status;

    /** 错误消息 */
    @ExcelProperty("错误消息")
    private String errorMsg;

    /** 操作时间 */
    @ExcelProperty("操作时间")
    private LocalDateTime operTime;

    /** 消耗时间（毫秒） */
    @ExcelProperty("消耗时间")
    private Long costTime;
}
