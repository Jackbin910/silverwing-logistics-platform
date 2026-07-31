package com.silverwing.biz.config.infrastructure.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.silverwing.biz.config.infrastructure.dao.po.SysConfigPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 参数配置数据访问层。
 */
@Mapper
public interface SysConfigDao extends BaseMapper<SysConfigPO> {
}
