package com.silverwing.biz.iam.infrastructure.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.silverwing.biz.iam.infrastructure.dao.po.SysPermissionPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysPermissionDao extends BaseMapper<SysPermissionPO> {

    List<String> selectPermissionCodesByUserId(@Param("userId") Long userId);
}
