package com.silverwing.biz.iam.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 角色权限关联（值对象）
 */
@Data
@TableName(value = "sys_role_permission")
public class SysRolePermission {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long roleId;
    private Long permissionId;

    public static SysRolePermission of(Long roleId, Long permissionId) {
        SysRolePermission rp = new SysRolePermission();
        rp.roleId = roleId;
        rp.permissionId = permissionId;
        return rp;
    }
}
