package com.silverwing.auth.iam.domain.model.aggregate;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.silverwing.common.entity.DomainEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 认证用户聚合根（auth 自有 IAM 领域模型）
 * <p>
 * 从 admin-web 的 IAM 领域溶解而来：auth 模块不再依赖 admin-web，
 * 自行持有用户聚合与密码校验等核心领域行为。
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AuthUserAggregate extends DomainEntity {

    private Long id;

    private String username;
    private Integer sex;
    private String password;
    private String avatar;
    private String phone;
    private String email;

    /** 状态: 0-禁用, 1-启用 */
    private Integer status;

    /** 用户是否处于启用状态 */
    public boolean isActive() {
        return status != null && status == 1;
    }

    /**
     * 校验明文密码是否匹配已存储的 BCrypt 哈希
     *
     * @param rawPassword 明文密码
     * @return 是否匹配
     */
    public boolean matchesPassword(String rawPassword) {
        if (CharSequenceUtil.isBlank(password)) {
            return false;
        }
        return BCrypt.checkpw(rawPassword, password);
    }

    /** 清除密码字段（避免泄露） */
    public void clearPassword() {
        this.password = null;
    }
}
