package com.silverwing.biz.iam.infrastructure.dao.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.silverwing.common.entity.BaseLogicEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 权限持久化对象（PO），对应 sys_permission 表。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_permission")
public class SysPermissionPO extends BaseLogicEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 权限标识（如 system:user:list） */
    private String permissionCode;

    /** 权限名称 */
    private String permissionName;

    /** 菜单类型（M目录 C菜单 F按钮） */
    private String resourceType;

    /** 父级ID，0为顶级 */
    private Long parentId;

    /** 排序 */
    private Integer sort;

    /** 状态: 0-禁用, 1-启用 */
    private Integer status;

    /** 是否可见: 0-可见, 1-隐藏 */
    private Integer visible;

    private Integer isFrame;

    private String icon;

    private String component;

    private String query;

    private String routeName;

    private Integer isCache;

    private String perms;

}
