package com.silverwing.common.operlog;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志数据传输对象
 * <p>
 * 字段与 sys_oper_log 表一一对应，由各微服务记录器转换为本库 PO 后落库。
 * </p>
 */
@Data
public class OperLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 模块标题 */
    private String title;

    /** 业务类型（0其它 1新增 2修改 3删除） */
    private Integer businessType;

    /** 方法名称（全限定方法名） */
    private String method;

    /** 请求方式（GET/POST...） */
    private String requestMethod;

    /** 操作类别（0其它 1后台用户 2手机端用户） */
    private Integer operatorType;

    /** 操作人员 */
    private String operName;

    /** 部门名称 */
    private String deptName;

    /** 请求URL */
    private String operUrl;

    /** 主机地址 */
    private String operIp;

    /** 操作地点 */
    private String operLocation;

    /** 请求参数 */
    private String operParam;

    /** 返回参数 */
    private String jsonResult;

    /** 操作状态（0正常 1异常） */
    private Integer status;

    /** 错误消息 */
    private String errorMsg;

    /** 操作时间 */
    private LocalDateTime operTime;

    /** 消耗时间（毫秒） */
    private Long costTime;
}
