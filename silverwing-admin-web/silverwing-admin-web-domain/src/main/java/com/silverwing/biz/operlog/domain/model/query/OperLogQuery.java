package com.silverwing.biz.operlog.domain.model.query;

import com.silverwing.common.domain.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 操作日志查询条件（领域层）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OperLogQuery extends PageRequest {

    /** 操作模块（模糊） */
    private String title;

    /** 操作人员（模糊） */
    private String operName;

    /** 业务类型（0其它 1新增 2修改 3删除 4导出 5导入） */
    private Integer businessType;

    /** 操作状态（0正常 1异常） */
    private Integer status;

    /** 开始时间（按操作时间查询） */
    private String beginTime;

    /** 结束时间（按操作时间查询） */
    private String endTime;
}
