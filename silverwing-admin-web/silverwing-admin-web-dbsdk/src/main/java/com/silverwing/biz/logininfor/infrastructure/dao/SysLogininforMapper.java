package com.silverwing.biz.logininfor.infrastructure.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.silverwing.biz.logininfor.infrastructure.dao.po.SysLogininforPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统访问记录数据访问层。
 * <p>复杂查询（按状态/时间排序导出）通过 {@code SysLogininforMapper.xml} 承载，
 * 简单条件构造交由 MyBatis-Plus {@link BaseMapper} 完成。</p>
 *
 * @author silverwing
 */
@Mapper
public interface SysLogininforMapper extends BaseMapper<SysLogininforPO> {
}
