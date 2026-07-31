package com.silverwing.biz.iam.domain.model.aggregate;

import com.silverwing.common.entity.DomainEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 权限聚合根
 * <p>
 * 封装权限的领域行为：资源类型判断、启用/禁用。
 * 支持菜单（menu）、按钮（button）、接口（api）三种资源类型。
 * 持久化映射由基础设施层的 SysPermissionPO（@TableName）承担，聚合根本身不持有表注解。
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysPermissionAggregate extends DomainEntity {

    private Long id;

    /** 权限标识（如 system:user:list） */
    private String permissionCode;

    /** 权限名称 */
    private String permissionName;

    /** 资源类型: menu-菜单, button-按钮, api-接口 */
    private String resourceType;

    /** 父级ID，0为顶级 */
    private Long parentId;

    /** 排序 */
    private Integer sort;

    /** 状态: 0-禁用, 1-启用 */
    private Integer status;

    /** 是否可见: 0-可见, 1-隐藏 */
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

    // ===== 领域行为 =====

    public boolean isActive() {
        return status != null && status == 1;
    }

    public void enable() {
        this.status = 1;
    }

    public void disable() {
        this.status = 0;
    }

    public boolean isMenu() {
        return "menu".equals(resourceType);
    }

    public boolean isButton() {
        return "button".equals(resourceType);
    }

    public boolean isApi() {
        return "api".equals(resourceType);
    }

    public boolean isTopLevel() {
        return parentId == null || parentId == 0L;
    }

    public boolean isVisible() {
        return visible != null && visible == 0;
    }
}
