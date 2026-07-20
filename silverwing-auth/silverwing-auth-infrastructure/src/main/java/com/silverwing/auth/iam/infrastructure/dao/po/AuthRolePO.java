package com.silverwing.auth.iam.infrastructure.dao.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.silverwing.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色持久化对象（PO），对应 sys_role 表。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_role")
public class AuthRolePO extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 角色编码（唯一标识，如 ADMIN、USER） */
    private String roleCode;

    /** 角色名称 */
    private String roleName;

    /** 状态: 0-禁用, 1-启用 */
    private Integer status;
}
