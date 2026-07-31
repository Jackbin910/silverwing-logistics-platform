package com.silverwing.admin.application.command;

import com.silverwing.admin.application.dto.RoleResponse;
import com.silverwing.admin.client.IamRoleClient;
import com.silverwing.admin.client.IamUserClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 角色命令服务（CQRS 写侧）
 * <p>仅做用例编排，通过 {@link IamRoleClient} 防腐层端口访问 biz-iam 角色上下文。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleCommandService {

    private final IamRoleClient iamRoleClient;
    private final IamUserClient iamUserClient;

    public RoleResponse create(SaveRoleCommand command) {
        return iamRoleClient.create(command);
    }

    public void update(Long id, SaveRoleCommand command) {
        iamRoleClient.update(id, command);
    }

    public void delete(Long id) {
        iamRoleClient.delete(id);
    }

    public void assignPermissions(Long roleId, List<Long> permissionIds) {
        iamRoleClient.assignPermissions(roleId, permissionIds);
    }

    public void changeStatus(Long roleId, Integer status) {
        iamRoleClient.changeStatus(roleId, status);
    }

    public void updateDataScope(Long roleId, Integer dataScope, List<Long> deptIds) {
        iamRoleClient.updateDataScope(roleId, dataScope, deptIds);
    }

    public void deleteByIds(List<Long> ids) {
        iamRoleClient.deleteByIds(ids);
    }

    /**
     * 取消角色与单个用户的授权
     */
    public void cancelAuthUser(Long roleId, Long userId) {
        iamUserClient.removeRoleFromUser(roleId, userId);
    }

    /**
     * 批量取消角色与用户的授权
     */
    public void cancelAuthUsers(Long roleId, List<Long> userIds) {
        iamUserClient.removeRolesFromUser(roleId, userIds);
    }

    /**
     * 批量为角色授予用户
     */
    public void selectAuthUsers(Long roleId, List<Long> userIds) {
        iamUserClient.addRoleToUsers(roleId, userIds);
    }
}
