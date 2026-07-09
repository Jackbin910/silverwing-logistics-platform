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

    private String createBy;

    private String updateBy;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
