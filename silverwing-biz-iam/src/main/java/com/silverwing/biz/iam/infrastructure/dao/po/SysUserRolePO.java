package com.silverwing.biz.iam.infrastructure.dao.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 用户角色关联持久化对象（PO），对应 sys_user_role 表。
 */
@Data
@TableName(value = "sys_user_role")
public class SysUserRolePO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long roleId;
}
