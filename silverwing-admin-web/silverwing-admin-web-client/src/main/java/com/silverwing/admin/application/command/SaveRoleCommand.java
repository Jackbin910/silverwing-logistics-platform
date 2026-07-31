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

    /** 数据范围（1：全部 2：自定数据权限 3：本部门 4：本部门及以下） */
    private Integer dataScope;

    /** 显示顺序 */
    private Integer roleSort;

    /** 菜单树选择项是否关联显示（1是 0否） */
    private Integer menuCheckStrictly;

    /** 部门树选择项是否关联显示（1是 0否） */
    private Integer deptCheckStrictly;
}
