package com.silverwing.biz.iam.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.silverwing.common.entity.BaseEntity;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户聚合根
 * <p>
 * 封装用户的核心领域行为：启用/禁用、密码变更、状态判断。
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_user")
public class SysUser extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;
    private Integer sex;
    private String password;
    private String avatar;
    private String phone;
    private String email;

    /** 状态: 0-禁用, 1-启用 */
    private Integer status;

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
     * 修改密码（传入明文密码，内部使用 MD5 + 随机盐 加密后存储）
     * <p>
     * 盐值随机生成并写入 salt 字段，password 字段存储 MD5(salt + 明文) 的小写十六进制串。
     * </p>
     * @param rawPassword 明文密码
     */
    public void changePassword(String rawPassword) {
        String salt = RandomUtil.randomString(16);
        this.salt = salt;
        this.password = encrypt(rawPassword, salt);
    }

    /**
     * 校验明文密码是否匹配已存储的 MD5 + 盐哈希
     * @param rawPassword 明文密码
     * @return 是否匹配
     */
    public boolean matchesPassword(String rawPassword) {
        if (salt == null || salt.isBlank() || password == null || password.isBlank()) {
            return false;
        }
        return password.equalsIgnoreCase(encrypt(rawPassword, salt));
    }

    /** 计算 MD5(salt + 明文) 的小写十六进制串 */
    private static String encrypt(String rawPassword, String salt) {
        return DigestUtil.md5Hex(salt + rawPassword);
    }

    /** 清除密码字段（用于 DTO 转换，避免泄露） */
    public void clearPassword() {
        this.password = null;
    }
}
