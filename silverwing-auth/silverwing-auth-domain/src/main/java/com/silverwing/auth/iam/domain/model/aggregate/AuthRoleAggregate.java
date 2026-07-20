package com.silverwing.auth.iam.domain.model.aggregate;

import com.silverwing.common.entity.DomainEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 认证角色聚合根（auth 自有 IAM 领域模型）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AuthRoleAggregate extends DomainEntity {

    private Long id;

    /** 角色编码（唯一标识，如 ADMIN、USER） */
    private String roleCode;

    /** 角色名称 */
    private String roleName;

    /** 状态: 0-禁用, 1-启用 */
    private Integer status;

    /** 角色是否处于启用状态 */
    public boolean isActive() {
        return status != null && status == 1;
    }
}
