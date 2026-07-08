package com.silverwing.common.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.silverwing.common.entity.BaseEntity;
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
     * 修改密码（传入已加密的密码哈希）
     * @param encryptedPassword BCrypt 加密后的密码
     */
    public void changePassword(String encryptedPassword) {
        this.password = encryptedPassword;
    }

    /** 清除密码字段（用于 DTO 转换，避免泄露） */
    public void clearPassword() {
        this.password = null;
    }
}
