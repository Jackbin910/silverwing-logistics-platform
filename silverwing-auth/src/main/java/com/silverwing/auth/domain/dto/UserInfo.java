package com.silverwing.auth.domain.dto;

import lombok.Data;

import java.util.List;

/**
 * 用户信息 DTO
 */
@Data
public class UserInfo {
    /**
     * 用户 ID
     */
    private Long id;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 用户昵称
     */
    private String nickname;
    
    /**
     * 用户角色列表
     */
    private List<String> roles;
    
    /**
     * 头像 URL
     */
    private String avatar;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 手机号
     */
    private String phone;

    /**
     * 用户权限标识列表
     */
    private List<String> permissions;
}
