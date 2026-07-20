package com.silverwing.biz.iam.domain.model.entity;

import lombok.Data;

/**
 * 角色权限关联（实体）
 * <p>持久化映射由基础设施层的 SysRolePermissionPO（@TableName）承担，关联实体本身不持有表注解。</p>
 */
@Data
public class SysRolePermission {

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
