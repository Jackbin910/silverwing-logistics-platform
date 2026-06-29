package com.silverwing.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统权限实体
 */
@Data
@TableName(value = "sys_permission", autoResultMap = true)
public class SysPermission {

    /**
     * 权限ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 权限标识（如 system:user:list）
     */
    private String permissionCode;

    /**
     * 权限名称（如 查询用户）
     */
    private String permissionName;

    /**
     * 资源类型: menu-菜单, button-按钮, api-接口
     */
    private String resourceType;

    /**
     * 父级ID，0为顶级
     */
    private Long parentId;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态: 0-禁用, 1-启用
     */
    private Integer status;

    /**
     * 删除标记
     */
    @TableLogic
    private Integer deleted;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

}
