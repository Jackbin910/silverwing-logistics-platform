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

    /** 路由地址 */
    private String path;

    /** 组件路径 */
    private String component;

    /** 路由参数 */
    private String query;

    /** 路由名称 */
    private String routeName;

    /** 是否为外链（0是 1否） */
    private Integer isFrame;

    /** 是否缓存（0缓存 1不缓存） */
    private Integer isCache;

    /** 权限标识（按钮/接口权限码） */
    private String perms;
}
