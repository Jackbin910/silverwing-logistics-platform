package com.silverwing.admin.application.command;

import com.silverwing.admin.application.dto.PermissionResponse;
import com.silverwing.admin.application.command.SavePermissionCommand;
import com.silverwing.admin.client.IamPermissionCacheClient;
import com.silverwing.admin.client.IamPermissionClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 权限命令服务（CQRS 写侧）
 * <p>仅做用例编排，通过 {@link IamPermissionClient} 与 {@link IamPermissionCacheClient}
 * 防腐层端口访问 biz-iam 权限上下文与 Sa-Token 缓存，应用层本身不感知聚合根、仓储与
 * 缓存刷新的具体实现。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionCommandService {

    private final IamPermissionClient iamPermissionClient;
    private final IamPermissionCacheClient iamPermissionCacheClient;

    public PermissionResponse create(SavePermissionCommand command) {
        return iamPermissionClient.create(command);
    }

    public void update(Long id, SavePermissionCommand command) {
        iamPermissionClient.update(id, command);
    }

    public void delete(Long id) {
        iamPermissionClient.delete(id);
    }

    /**
     * 刷新指定用户的权限缓存
     */
    public void refreshUserPermissionCache(Long userId) {
        iamPermissionCacheClient.refreshUserPermissionCache(userId);
    }

    /**
     * 批量刷新角色下所有在线用户的权限缓存
     */
    public void refreshRoleUserCache(Long roleId) {
        iamPermissionCacheClient.refreshRoleUserCache(roleId);
    }
}
