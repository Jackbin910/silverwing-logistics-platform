package com.silverwing.admin.application.command;

import com.silverwing.admin.application.command.UpdateProfileCommand;
import com.silverwing.admin.application.command.UpdateProfilePasswordCommand;
import com.silverwing.admin.client.IamUserClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 个人中心命令服务（CQRS 写侧）
 * <p>仅做用例编排，通过 {@link IamUserClient} 防腐层端口访问 biz-iam 用户上下文，
 * 符合 DDD 防腐层（ACL）设计。</p>
 *
 * @author silverwing
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileCommandService {

    private final IamUserClient iamUserClient;

    /**
     * 修改个人资料
     *
     * @param userId  当前登录用户 ID
     * @param command 资料修改命令
     */
    public void updateProfile(Long userId, UpdateProfileCommand command) {
        iamUserClient.updateProfile(userId, command);
    }

    /**
     * 修改密码（需校验旧密码）
     *
     * @param userId  当前登录用户 ID
     * @param command 密码修改命令
     */
    public void updateSelfPassword(Long userId, UpdateProfilePasswordCommand command) {
        iamUserClient.updateSelfPassword(userId, command.getOldPassword(), command.getNewPassword());
    }

    /**
     * 更新头像地址
     *
     * @param userId    当前登录用户 ID
     * @param avatarUrl 头像访问地址
     */
    public void updateAvatar(Long userId, String avatarUrl) {
        iamUserClient.updateAvatar(userId, avatarUrl);
    }
}
