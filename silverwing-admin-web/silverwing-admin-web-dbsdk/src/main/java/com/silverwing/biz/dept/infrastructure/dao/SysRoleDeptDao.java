package com.silverwing.biz.dept.infrastructure.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.silverwing.biz.dept.infrastructure.dao.po.SysRoleDeptPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色部门关联数据访问层。
 */
@Mapper
public interface SysRoleDeptDao extends BaseMapper<SysRoleDeptPO> {
}
