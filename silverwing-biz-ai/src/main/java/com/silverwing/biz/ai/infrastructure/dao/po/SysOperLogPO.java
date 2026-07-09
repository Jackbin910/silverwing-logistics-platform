package com.silverwing.biz.ai.infrastructure.dao.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.silverwing.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 操作日志持久化对象（PO）
 * <p>
 * 与 silverwing_ai 库的 sys_oper_log 表一一对应，仅承载数据。由
 * {@code OperLogAspect} 直接组装后通过 {@code SysOperLogMapper#insertBatch} 落库。
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_oper_log")
public class SysOperLogPO extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;
    private Integer businessType;
    private String method;
    private String requestMethod;
    private Integer operatorType;
    private String operName;
    private String deptName;
    private String operUrl;
    private String operIp;
    private String operLocation;
    private String operParam;
    private String jsonResult;
    private Integer status;
    private String errorMsg;
    private LocalDateTime operTime;
    private Long costTime;
}
