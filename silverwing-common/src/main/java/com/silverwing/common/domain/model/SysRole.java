package com.silverwing.common.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.silverwing.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色聚合根
 * <p>
 * 封装角色的领域行为：启用/禁用、状态判断。
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_role", autoResultMap = true)
public class SysRole extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 角色编码（唯一标识，如 ADMIN、USER） */
    private String roleCode;

    /** 角色名称 */
    private String roleName;

    /** 状态: 0-禁用, 1-启用 */
    private Integer status;

    // ===== 领域行为 =====

    public boolean isActive() {
        return status != null && status == 1;
    }

    public void enable() {
        this.status = 1;
    }

    public void disable() {
        this.status = 0;
    }
}
