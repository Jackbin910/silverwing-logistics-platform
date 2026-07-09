package com.silverwing.biz.iam.infrastructure.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.silverwing.biz.iam.infrastructure.dao.po.SysRolePO;
import com.silverwing.biz.iam.infrastructure.dao.po.SysUserRolePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysUserRoleDao extends BaseMapper<SysUserRolePO> {

    List<SysRolePO> selectRolesByUserId(@Param("userId") Long userId);

    List<Long> selectUserIdsByRoleId(@Param("roleId") Long roleId);
}
