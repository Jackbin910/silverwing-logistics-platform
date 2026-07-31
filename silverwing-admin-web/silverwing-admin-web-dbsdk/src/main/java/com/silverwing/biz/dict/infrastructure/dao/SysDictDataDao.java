package com.silverwing.biz.dict.infrastructure.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.silverwing.biz.dict.infrastructure.dao.po.SysDictDataPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 字典数据数据访问对象，对应 sys_dict_data 表。
 */
@Mapper
public interface SysDictDataDao extends BaseMapper<SysDictDataPO> {
}
