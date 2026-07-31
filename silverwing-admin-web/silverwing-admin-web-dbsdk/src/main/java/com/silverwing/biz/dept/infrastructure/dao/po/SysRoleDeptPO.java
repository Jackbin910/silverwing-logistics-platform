package com.silverwing.biz.dept.infrastructure.dao.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 角色与部门关联持久化对象（PO），对应 sys_role_dept 表。
 */
@Data
@TableName(value = "sys_role_dept")
public class SysRoleDeptPO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 角色ID */
    private Long roleId;

    /** 部门ID */
    private Long deptId;
}
