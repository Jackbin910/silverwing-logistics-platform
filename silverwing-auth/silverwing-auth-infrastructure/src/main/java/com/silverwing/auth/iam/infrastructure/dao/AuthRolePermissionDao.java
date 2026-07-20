package com.silverwing.auth.iam.infrastructure.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.silverwing.auth.iam.infrastructure.dao.po.AuthRolePermissionPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色权限关联数据访问对象（MyBatis-Plus）
 */
@Mapper
public interface AuthRolePermissionDao extends BaseMapper<AuthRolePermissionPO> {
}
