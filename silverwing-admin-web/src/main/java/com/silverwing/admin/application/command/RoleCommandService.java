package com.silverwing.admin.application.command;

import com.silverwing.admin.application.dto.RoleResponse;
import com.silverwing.admin.client.IamRoleClient;
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
}
