package com.silverwing.admin.application.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 权限响应DTO
 * <p>由 SysPermissionAggregate 经 PermissionConvertor 映射得到，作为触发层对外返回的权限视图，不暴露领域聚合根。</p>
 */
@Data
public class PermissionResponse implements Serializable {

    private Long id;

    private String permissionCode;

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

    private String createBy;

    private String updateBy;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
