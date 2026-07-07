package com.silverwing.auth.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 登录响应 DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    /**
     * 认证 Token
     */
    private String token;
    
    /**
     * 用户名
     */
    private String username;

    
    /**
     * 用户角色列表
     */
    private List<String> roles;

}
