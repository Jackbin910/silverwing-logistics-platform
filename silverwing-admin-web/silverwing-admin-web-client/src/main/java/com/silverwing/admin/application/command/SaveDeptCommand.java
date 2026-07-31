package com.silverwing.admin.application.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 保存部门命令。
 */
@Data
@Schema(description = "保存部门命令")
public class SaveDeptCommand {

    @Schema(description = "部门主键，更新时必填")
    private Long deptId;

    @Schema(description = "父部门ID")
    private Long parentId;

    @Schema(description = "部门名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{admin.dept.name.required}")
    private String deptName;

    @Schema(description = "显示顺序", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{admin.dept.order.required}")
    private Integer orderNum;

    @Schema(description = "负责人")
    private String leader;

    @Schema(description = "联系电话")
    private String phone;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "部门状态（0-正常 1-停用）")
    private String status;
}
