package com.silverwing.admin.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 角色关联部门树响应。
 */
@Data
@Schema(description = "角色关联部门树响应")
public class DeptRoleTreeResponse {

    @Schema(description = "部门树")
    private List<DeptTreeResponse> depts;

    @Schema(description = "角色已关联的部门ID")
    private List<Long> checkedKeys;
}
