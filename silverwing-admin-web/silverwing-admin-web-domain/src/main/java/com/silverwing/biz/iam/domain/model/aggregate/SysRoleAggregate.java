package com.silverwing.biz.iam.domain.model.aggregate;

import com.silverwing.common.entity.DomainEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色聚合根
 * <p>
 * 封装角色的领域行为：启用/禁用、状态判断。
 * 持久化映射由基础设施层的 SysRolePO（@TableName）承担，聚合根本身不持有表注解。
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysRoleAggregate extends DomainEntity {

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
