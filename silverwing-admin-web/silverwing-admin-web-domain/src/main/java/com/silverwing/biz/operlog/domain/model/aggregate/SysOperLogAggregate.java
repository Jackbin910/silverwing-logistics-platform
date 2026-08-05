package com.silverwing.biz.operlog.domain.model.aggregate;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志聚合根。
 * 操作日志为系统记录型数据，无业务行为，仅承载字段与基础构造。
 */
@Data
public class SysOperLogAggregate {

    /** 日志主键ID */
    private Long operId;

    /** 操作模块 */
    private String title;

    /** 业务类型（0其它 1新增 2修改 3删除 4授权 5导出 6导入 7强退 8生成代码 9清空数据） */
    private Integer businessType;

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

    /** 请求方式 */
    private String requestMethod;

    /** 操作方法 */
    private String method;

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
