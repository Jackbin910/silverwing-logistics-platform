package com.silverwing.biz.dict.domain.adapter.repository;

import com.silverwing.biz.dict.domain.model.aggregate.SysDictTypeAggregate;
import com.silverwing.biz.dict.domain.model.query.DictTypeQuery;
import com.silverwing.common.domain.PageResult;

import java.util.List;

/**
 * 字典类型仓储接口。
 */
public interface DictTypeRepository {

    /** 分页查询字典类型 */
    PageResult<SysDictTypeAggregate> findPage(DictTypeQuery query);

    /** 条件查询全部字典类型（用于导出） */
    List<SysDictTypeAggregate> findList(DictTypeQuery query);

    /** 查询全部字典类型（用于下拉选项） */
    List<SysDictTypeAggregate> findAll();

    /** 根据主键查询字典类型 */
    SysDictTypeAggregate findById(Long dictId);

    /** 保存（新增或更新）字典类型 */
    void save(SysDictTypeAggregate aggregate);

    /** 根据主键删除字典类型 */
    void deleteById(Long dictId);

    /** 判断同字典类型编码是否已存在（排除指定主键） */
    boolean existsByDictType(String dictType, Long excludeId);
}
