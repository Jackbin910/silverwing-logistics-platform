package com.silverwing.common.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.silverwing.common.domain.model.SysUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
}
