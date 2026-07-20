package com.silverwing.auth.iam.infrastructure.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.silverwing.auth.iam.infrastructure.dao.po.AuthRolePO;
import com.silverwing.auth.iam.infrastructure.dao.po.AuthUserRolePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户角色关联数据访问对象（MyBatis-Plus）
 */
@Mapper
public interface AuthUserRoleDao extends BaseMapper<AuthUserRolePO> {

    /** 根据用户ID查询其关联的角色列表 */
    List<AuthRolePO> selectRolesByUserId(@Param("userId") Long userId);

    /** 根据角色ID查询关联的用户ID列表 */
    List<Long> selectUserIdsByRoleId(@Param("roleId") Long roleId);
}
