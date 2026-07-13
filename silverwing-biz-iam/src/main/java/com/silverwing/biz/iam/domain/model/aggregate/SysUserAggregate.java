package com.silverwing.biz.iam.domain.model.aggregate;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.silverwing.common.entity.DomainEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户聚合根
 * <p>
 * 封装用户的核心领域行为：启用/禁用、密码变更、状态判断。
 * 持久化映射由基础设施层的 SysUserPO（@TableName）承担，聚合根本身不持有表注解。
 * </p>
 *
 * @author silverwing
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysUserAggregate extends DomainEntity {

    private Long id;

    private String username;
    private Integer sex;
    private String password;
    private String avatar;
    private String phone;
    private String email;

    /** 状态: 0-禁用, 1-启用 */
    private Integer status;

    /** 密码盐值（BCrypt 模式下已废弃，盐值内嵌于 BCrypt 哈希中，此字段仅兼容旧表结构） */
    private String salt;

    // ===== 领域行为 =====

    /** 用户是否处于启用状态 */
    public boolean isActive() {
        return status != null && status == 1;
    }

    /** 启用用户 */
    public void enable() {
        this.status = 1;
    }

    /** 禁用用户 */
    public void disable() {
        this.status = 0;
    }

    /** 切换启用/禁用状态 */
    public void toggleStatus() {
        this.status = isActive() ? 0 : 1;
    }

    /**
     * 修改密码（传入明文密码，内部使用 BCrypt 加密后存储）
     * <p>
     * BCrypt 自带随机盐，盐值内嵌于哈希结果中（格式：$2a$10$...），
     * 无需单独维护 salt 字段。
     * </p>
     *
     * @param rawPassword 明文密码
     */
    public void changePassword(String rawPassword) {
        this.password = BCrypt.hashpw(rawPassword);
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

    /** 清除密码字段（用于 DTO 转换，避免泄露） */
    public void clearPassword() {
        this.password = null;
    }
}
