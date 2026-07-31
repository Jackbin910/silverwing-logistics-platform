package com.silverwing.admin.application.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 部门查询。
 */
@Data
@Schema(description = "部门查询")
public class DeptQuery {

    @Schema(description = "部门名称")
    private String deptName;

    @Schema(description = "部门状态（0-正常 1-停用）")
    private String status;
}
