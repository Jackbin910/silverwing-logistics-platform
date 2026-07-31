package com.silverwing.biz.dept.infrastructure.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.silverwing.biz.dept.infrastructure.dao.po.SysDeptPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 部门数据访问层。
 */
@Mapper
public interface SysDeptDao extends BaseMapper<SysDeptPO> {
}
