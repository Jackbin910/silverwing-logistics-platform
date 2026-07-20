package com.silverwing.auth.iam.infrastructure.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.silverwing.auth.iam.infrastructure.dao.po.AuthPermissionPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 权限数据访问对象（MyBatis-Plus）
 */
@Mapper
public interface AuthPermissionDao extends BaseMapper<AuthPermissionPO> {

    /** 根据用户ID查询其拥有的权限标识列表（用户 -> 角色 -> 权限） */
    List<String> selectPermissionCodesByUserId(@Param("userId") Long userId);
}
