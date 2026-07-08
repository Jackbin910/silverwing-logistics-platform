package com.silverwing.common.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.silverwing.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 权限聚合根
 * <p>
 * 封装权限的领域行为：资源类型判断、启用/禁用。
 * 支持菜单（menu）、按钮（button）、接口（api）三种资源类型。
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_permission")
public class SysPermission extends BaseEntity {

    @TableId(type = IdType.AUTO)
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
