package com.silverwing.biz.iam.infrastructure.dao.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.silverwing.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 权限持久化对象（PO），对应 sys_permission 表。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_permission")
public class SysPermissionPO extends BaseEntity {

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
}
