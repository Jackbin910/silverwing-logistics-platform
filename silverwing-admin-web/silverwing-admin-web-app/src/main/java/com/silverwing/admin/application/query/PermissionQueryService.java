package com.silverwing.admin.application.query;

import com.silverwing.admin.application.dto.PermissionResponse;
import com.silverwing.admin.application.dto.RolePermissionTreeSelectResponse;
import com.silverwing.admin.application.dto.RouterVo;
import com.silverwing.admin.application.dto.TreeSelect;
import com.silverwing.admin.application.query.PermissionPageQuery;
import com.silverwing.common.domain.PageResult;

import java.util.List;

/**
 * 权限查询服务（CQRS 读侧）
 * <p>通过 {@link com.silverwing.admin.client.IamPermissionClient} 防腐层端口访问 biz-iam，
 * 返回本模块 {@link PermissionResponse}，避免直接暴露领域聚合根。</p>
 */
public interface PermissionQueryService {

    List<PermissionResponse> listAll();

    /**
     * 分页查询权限列表
     */
    PageResult<PermissionResponse> page(PermissionPageQuery query);

    PermissionResponse getById(Long id);

    /**
     * 权限树形下拉列表
     */
    List<TreeSelect> treeSelect(PermissionPageQuery query);

    /**
     * 角色关联权限树（含已勾选权限ID）
     */
    RolePermissionTreeSelectResponse rolePermissionTreeSelect(Long roleId);

    /**
     * 获取登录用户前端路由菜单
     */
    List<RouterVo> getRouters(Long userId);
}
