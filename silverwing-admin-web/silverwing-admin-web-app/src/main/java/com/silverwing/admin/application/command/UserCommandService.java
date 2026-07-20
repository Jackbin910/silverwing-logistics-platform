package com.silverwing.admin.application.command;

import com.silverwing.admin.application.dto.UserResponse;
import com.silverwing.admin.client.IamUserClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户命令服务（CQRS 写侧）
 * <p>仅做用例编排，通过 {@link IamUserClient} 防腐层端口访问 biz-iam 用户上下文，
 * 不再直接依赖其领域聚合根、仓储与领域服务，符合 DDD 防腐层（ACL）设计。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserCommandService {

    private final IamUserClient iamUserClient;

    public UserResponse create(CreateUserCommand command) {
        return iamUserClient.create(command);
    }

    public void update(Long id, UpdateUserCommand command) {
        iamUserClient.update(id, command);
    }

    public void delete(Long id) {
        iamUserClient.delete(id);
    }

    public void resetPassword(Long id, String newPassword) {
        iamUserClient.resetPassword(id, newPassword);
    }

    public void toggleStatus(Long id) {
        iamUserClient.toggleStatus(id);
    }

    public void assignRoles(Long userId, List<Long> roleIds) {
        iamUserClient.assignRoles(userId, roleIds);
    }
}
