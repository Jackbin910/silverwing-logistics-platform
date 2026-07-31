package com.silverwing.biz.dict.domain.adapter.repository;

import com.silverwing.biz.dict.domain.model.aggregate.SysDictDataAggregate;
import com.silverwing.biz.dict.domain.model.query.DictDataQuery;
import com.silverwing.common.domain.PageResult;

import java.util.List;

/**
 * 字典数据仓储接口。
 */
public interface DictDataRepository {

    /** 分页查询字典数据 */
    PageResult<SysDictDataAggregate> findPage(DictDataQuery query);

    /** 条件查询字典数据（用于导出） */
    List<SysDictDataAggregate> findList(DictDataQuery query);

    /** 根据主键查询字典数据 */
    SysDictDataAggregate findById(Long dictCode);

    /** 根据字典类型查询字典数据（按排序升序） */
    List<SysDictDataAggregate> findByDictType(String dictType);

    /** 保存（新增或更新）字典数据 */
    void save(SysDictDataAggregate aggregate);

    /** 根据主键删除字典数据 */
    void deleteById(Long dictCode);

    /** 判断同一字典类型下键值是否已存在（排除指定主键） */
    boolean existsByDictTypeAndValue(String dictType, String dictValue, Long excludeId);
}
