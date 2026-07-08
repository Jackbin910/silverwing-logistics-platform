package com.silverwing.common.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.silverwing.common.domain.model.SysRolePermission;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysRolePermissionMapper extends BaseMapper<SysRolePermission> {
}
