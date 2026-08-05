package com.silverwing.auth.iam.infrastructure.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.silverwing.auth.iam.infrastructure.dao.po.SysLogininforPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统访问记录数据访问对象（MyBatis-Plus）。
 *
 * @author silverwing
 */
@Mapper
public interface SysLogininforDao extends BaseMapper<SysLogininforPO> {
}
