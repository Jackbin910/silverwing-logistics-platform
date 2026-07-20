package com.silverwing.admin.client;

import com.silverwing.admin.application.command.SaveRoleCommand;
import com.silverwing.admin.application.dto.RoleResponse;
import com.silverwing.admin.application.query.RolePageQuery;
import com.silverwing.common.domain.PageResult;

import java.util.List;

/**
 * IAM 角色上下文防腐层端口
 * <p>应用层通过该端口访问 biz-iam 角色上下文，隔离对聚合根、仓储与领域服务的直接依赖。</p>
 */
public interface IamRoleClient {

    /**
     * 创建角色
     */
    RoleResponse create(SaveRoleCommand command);

    /**
     * 更新角色信息
     */
    void update(Long id, SaveRoleCommand command);

    /**
     * 删除角色
     */
    void delete(Long id);

    /**
     * 为角色分配权限（全量覆盖）
     */
    void assignPermissions(Long roleId, List<Long> permissionIds);

    /**
     * 分页查询角色列表
     */
    PageResult<RoleResponse> list(RolePageQuery query);

    /**
     * 查询全部启用角色
     */
    List<RoleResponse> listAllEnabled();

    /**
     * 根据ID查询角色
     */
    RoleResponse getById(Long id);

    /**
     * 查询角色已分配的权限ID列表
     */
    List<Long> getRolePermissionIds(Long roleId);
}
