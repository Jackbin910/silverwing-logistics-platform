package com.silverwing.admin.application.command;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 角色状态切换命令
 */
@Data
public class ChangeRoleStatusCommand {

    @NotNull(message = "角色ID不能为空")
    private Long roleId;

    @NotNull(message = "角色状态不能为空")
    private Integer status;
}
