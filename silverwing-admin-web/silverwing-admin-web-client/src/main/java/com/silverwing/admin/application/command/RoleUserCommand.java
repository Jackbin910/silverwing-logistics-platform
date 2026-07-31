package com.silverwing.admin.application.command;

import lombok.Data;

import java.util.List;

/**
 * 角色-用户授权命令
 * <p>用于取消授权、批量取消授权与批量授予授权场景。</p>
 */
@Data
public class RoleUserCommand {

    /** 角色ID */
    private Long roleId;

    /** 单个用户ID（取消授权场景） */
    private Long userId;

    /** 多个用户ID（批量授权/取消授权场景） */
    private List<Long> userIds;
}
