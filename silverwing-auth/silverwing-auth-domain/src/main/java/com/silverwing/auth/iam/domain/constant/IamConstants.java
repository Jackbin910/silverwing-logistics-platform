package com.silverwing.auth.iam.domain.constant;

import com.silverwing.common.constant.SaSessionConstants;

/**
 * IAM 领域常量（与 RuoYi 对应）
 */
public final class IamConstants {

    /** 超级管理员角色标识 */
    public static final String SUPER_ADMIN = "admin";

    /** 超级管理员拥有的全量权限标识（通配符），复用 common 定义 */
    public static final String ALL_PERMISSION = SaSessionConstants.ALL_PERMISSION;

    private IamConstants() {
    }
}
