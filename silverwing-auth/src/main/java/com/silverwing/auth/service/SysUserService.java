package com.silverwing.auth.service;

import com.silverwing.auth.entity.SysUser;

/**
 * 用户服务接口
 */
public interface SysUserService {

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户实体，不存在则返回 null
     */
    SysUser getByUsername(String username);

    /**
     * 根据ID查询用户
     *
     * @param id 用户ID
     * @return 用户实体，不存在则返回 null
     */
    SysUser getById(Long id);

}
