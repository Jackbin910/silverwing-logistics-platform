package com.silverwing.biz.dict.domain.service;

import com.silverwing.biz.dict.domain.model.aggregate.SysDictDataAggregate;
import com.silverwing.biz.dict.domain.model.aggregate.SysDictTypeAggregate;

/**
 * 字典领域服务，负责字典类型与字典数据的唯一性校验及持久化。
 */
public interface IDictDomainService {

    /** 保存字典类型（含编码唯一性校验） */
    void saveDictType(SysDictTypeAggregate aggregate);

    /** 删除字典类型 */
    void deleteDictTypeById(Long dictId);

    /** 保存字典数据（含键值唯一性校验） */
    void saveDictData(SysDictDataAggregate aggregate);

    /** 删除字典数据 */
    void deleteDictDataById(Long dictCode);
}
