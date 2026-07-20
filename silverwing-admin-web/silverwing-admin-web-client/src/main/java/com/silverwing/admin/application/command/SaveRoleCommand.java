package com.silverwing.admin.application.command;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 创建/更新角色命令
 */
@Data
public class SaveRoleCommand {

    @NotBlank(message = "{validation.role.rolecode.notblank}")
    private String roleCode;

    @NotBlank(message = "{validation.role.rolename.notblank}")
    private String roleName;

    private Integer status;
}
