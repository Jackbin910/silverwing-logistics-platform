package com.silverwing.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.silverwing.auth.entity.SysPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统权限 Mapper
 */
@Mapper
public interface SysPermissionMapper extends BaseMapper<SysPermission> {

    /**
     * 根据用户ID查询其拥有的权限标识列表（去重）
     *
     * @param userId 用户ID
     * @return 权限标识列表，如 ["system:user:list", "logistics:order:create"]
     */
    List<String> selectPermissionCodesByUserId(@Param("userId") Long userId);

}
