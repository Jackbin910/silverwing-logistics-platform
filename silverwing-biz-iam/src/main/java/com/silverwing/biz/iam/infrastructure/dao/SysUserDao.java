package com.silverwing.biz.iam.infrastructure.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.silverwing.biz.iam.infrastructure.dao.po.SysUserPO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysUserDao extends BaseMapper<SysUserPO> {
}
