package com.silverwing.common.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 用户角色关联（值对象）
 */
@Data
@TableName(value = "sys_user_role")
public class SysUserRole {

    @TableId(type = IdType.AUTO)
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
