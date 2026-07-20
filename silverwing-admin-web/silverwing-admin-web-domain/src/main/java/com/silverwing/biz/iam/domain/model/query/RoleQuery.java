package com.silverwing.biz.iam.domain.model.query;

import com.silverwing.common.domain.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色分页查询条件
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RoleQuery extends PageRequest {

    private String roleName;
    private Integer status;
}
