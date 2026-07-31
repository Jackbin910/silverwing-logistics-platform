package com.silverwing.admin.application.command;

import lombok.Data;

import java.util.List;

/**
 * 角色数据范围更新命令
 */
@Data
public class UpdateRoleDataScopeCommand {

    private Long roleId;

    private Integer dataScope;

    private List<Long> deptIds;
}
