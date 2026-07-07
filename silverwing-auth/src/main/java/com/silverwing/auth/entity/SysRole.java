package com.silverwing.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.silverwing.common.entity.BaseEntity;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统角色实体
 */
@Data
@TableName(value = "sys_role", autoResultMap = true)
public class SysRole extends BaseEntity {

    /**
     * 角色ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 角色编码（唯一标识，如：ADMIN、USER）
     */
    private String roleCode;

    /**
     * 角色名称（如：管理员、普通用户）
     */
    private String roleName;

    /**
     * 状态: 0-禁用, 1-启用
     */
    private Integer status;
}
