package com.silverwing.admin.application.command;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 创建/更新权限命令
 */
@Data
public class SavePermissionCommand {

    @NotBlank(message = "{validation.permission.permissioncode.notblank}")
    private String permissionCode;

    @NotBlank(message = "{validation.permission.permissionname.notblank}")
    private String permissionName;

    private String resourceType;
    private Long parentId;
    private Integer sort;
    private Integer status;
    private Integer visible;
    private String url;
    private String target;
    private Integer isRefresh;
    private String icon;
}
