package com.silverwing.auth.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 当前登录用户信息 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthUserInfo {

    private Long id;
    private String username;
    private String avatar;
    private String email;
    private String phone;
    private List<String> roles;
    private List<String> permissions;
}
