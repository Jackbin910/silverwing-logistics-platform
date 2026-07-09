package com.silverwing.biz.iam.domain.model.entity;

import lombok.Data;

/**
 * 用户角色关联（实体）
 * <p>持久化映射由基础设施层的 SysUserRolePO（@TableName）承担，关联实体本身不持有表注解。</p>
 */
@Data
public class SysUserRole {

    private Long id;

    private Long userId;
    private Long roleId;

    public static SysUserRole of(Long userId, Long roleId) {
        SysUserRole ur = new SysUserRole();
        ur.userId = userId;
        ur.roleId = roleId;
        return ur;
    }
}
