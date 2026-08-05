package com.silverwing.admin.application.query;

import com.silverwing.common.domain.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 操作日志分页查询条件。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OperLogPageQuery extends PageRequest {

    @Schema(description = "操作模块（模糊）")
    private String title;

    @Schema(description = "操作人员（模糊）")
    private String operName;

    @Schema(description = "业务类型")
    private Integer businessType;

    @Schema(description = "操作状态（0正常 1异常）")
    private Integer status;

    @Schema(description = "操作时间-开始")
    private String beginTime;

    @Schema(description = "操作时间-结束")
    private String endTime;
}
