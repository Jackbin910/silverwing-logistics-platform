package com.silverwing.admin.application.command;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 创建/更新角色命令
 */
@Data
public class SaveRoleCommand {

    @NotBlank(message = "角色编码不能为空")
    private String roleCode;

    @NotBlank(message = "角色名称不能为空")
    private String roleName;

    private Integer status;
}
