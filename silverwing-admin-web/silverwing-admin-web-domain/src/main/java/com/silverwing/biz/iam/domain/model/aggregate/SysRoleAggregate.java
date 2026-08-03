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

    /** 状态: 0-启用, 1-禁用 */
    private Integer status;

    /** 数据范围（1：全部 2：自定数据权限 3：本部门 4：本部门及以下） */
    private Integer dataScope;

    /** 显示顺序 */
    private Integer roleSort;

    /** 菜单树选择项是否关联显示（1是 0否） */
    private Integer menuCheckStrictly;

    /** 部门树选择项是否关联显示（1是 0否） */
    private Integer deptCheckStrictly;

    // ===== 领域行为 =====

    public boolean isActive() {
        return status != null && status == 0;
    }

    public void enable() {
        this.status = 0;
    }

    public void disable() {
        this.status = 1;
    }
}
