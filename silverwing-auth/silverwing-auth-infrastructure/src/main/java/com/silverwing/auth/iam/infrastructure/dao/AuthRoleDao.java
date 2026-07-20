package com.silverwing.auth.iam.infrastructure.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.silverwing.auth.iam.infrastructure.dao.po.AuthRolePO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色数据访问对象（MyBatis-Plus）
 */
@Mapper
public interface AuthRoleDao extends BaseMapper<AuthRolePO> {
}
