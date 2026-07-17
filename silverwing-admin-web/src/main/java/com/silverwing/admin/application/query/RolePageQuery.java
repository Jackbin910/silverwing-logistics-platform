package com.silverwing.admin.application.query;

import com.silverwing.common.domain.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色分页查询条件（admin-web 自有）
 * <p>替代对 biz-iam 领域查询对象 {@code RoleQuery} 的直接依赖，
 * 由防腐层适配器在调用 IAM 上下文时翻译为对方查询对象，保持应用层与领域层解耦。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RolePageQuery extends PageRequest {

    private String roleName;

    private Integer status;
}
