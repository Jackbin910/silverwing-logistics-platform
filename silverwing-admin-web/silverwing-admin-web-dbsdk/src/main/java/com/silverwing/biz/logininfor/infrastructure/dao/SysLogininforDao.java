package com.silverwing.biz.logininfor.infrastructure.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.silverwing.biz.logininfor.infrastructure.dao.po.SysLogininforPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统访问记录数据访问层。
 */
@Mapper
public interface SysLogininforDao extends BaseMapper<SysLogininforPO> {
}
