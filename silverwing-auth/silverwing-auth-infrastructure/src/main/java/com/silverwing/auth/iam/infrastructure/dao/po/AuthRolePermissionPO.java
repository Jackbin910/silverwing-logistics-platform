package com.silverwing.auth.iam.infrastructure.dao.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 角色权限关联持久化对象（PO），对应 sys_role_permission 表。
 */
@Data
@TableName(value = "sys_role_permission")
public class AuthRolePermissionPO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long roleId;
    private Long permissionId;
}
