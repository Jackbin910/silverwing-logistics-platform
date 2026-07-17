package com.silverwing.admin.client;

import com.silverwing.admin.application.command.SavePermissionCommand;
import com.silverwing.admin.application.dto.PermissionResponse;

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
     * 根据ID查询权限
     */
    PermissionResponse getById(Long id);
}
