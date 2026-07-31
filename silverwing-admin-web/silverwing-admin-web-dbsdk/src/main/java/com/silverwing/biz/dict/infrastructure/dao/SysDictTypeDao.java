package com.silverwing.biz.dict.infrastructure.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.silverwing.biz.dict.infrastructure.dao.po.SysDictTypePO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 字典类型数据访问对象，对应 sys_dict_type 表。
 */
@Mapper
public interface SysDictTypeDao extends BaseMapper<SysDictTypePO> {
}
