package com.silverwing.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.silverwing.common.entity.BaseEntity;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统用户实体
 */
@Data
@TableName(value = "sys_user", autoResultMap = true)
public class SysUser extends BaseEntity {
    /**
     * 用户ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 性别 0-男, 1-女, 2-未知
     */
    private Integer sex;

    /**
     * 密码（BCrypt 加密）
     */
    private String password;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 状态: 0-禁用, 1-启用
     */
    private Integer status;

}
