package com.silverwing.admin.client;

import com.silverwing.admin.application.command.SavePermissionCommand;
import com.silverwing.admin.application.command.UpdatePermissionSortCommand;
import com.silverwing.admin.application.dto.PermissionResponse;
import com.silverwing.admin.application.dto.RolePermissionTreeSelectResponse;
import com.silverwing.admin.application.dto.RouterVo;
import com.silverwing.admin.application.dto.TreeSelect;
import com.silverwing.admin.application.query.PermissionPageQuery;
import com.silverwing.common.domain.PageResult;

import java.util.List;

/**
 * IAM 权限上下文防腐层端口
 * <p>应用层通过该端口访问 biz-iam 权限上下文，隔离对聚合根、仓储与领域服务的直接依赖。</p>
 */
public interface IamPermissionClient {

    /**
     * 创建权限
     */
    PermissionResponse create(SavePermissionCommand command);

    /**
     * 更新权限
     */
    void update(Long id, SavePermissionCommand command);

    /**
     * 删除权限
     */
    void delete(Long id);

    /**
     * 查询全部权限
     */
    List<PermissionResponse> listAll();

    /**
     * 分页查询权限列表
     */
    PageResult<PermissionResponse> page(PermissionPageQuery query);

    /**
     * 根据ID查询权限
     */
    PermissionResponse getById(Long id);

    /**
     * 权限树形下拉列表（菜单管理树选择）
     */
    List<TreeSelect> treeSelect(PermissionPageQuery query);

    /**
     * 角色关联权限树（已勾选权限ID + 完整权限树）
     */
    RolePermissionTreeSelectResponse rolePermissionTreeSelect(Long roleId);

    /**
     * 保存权限（菜单）排序
     */
    void updateSort(UpdatePermissionSortCommand command);

    /**
     * 获取登录用户的前端路由菜单
     */
    List<RouterVo> getRouters(Long userId);
}
