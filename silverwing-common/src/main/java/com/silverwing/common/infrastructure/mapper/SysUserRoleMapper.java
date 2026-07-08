package com.silverwing.common.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.silverwing.common.domain.model.SysRole;
import com.silverwing.common.domain.model.SysUserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {

    List<SysRole> selectRolesByUserId(@Param("userId") Long userId);

    List<Long> selectUserIdsByRoleId(@Param("roleId") Long roleId);
}
