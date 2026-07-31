package com.silverwing.biz.config.domain.adapter.repository;

import com.silverwing.biz.config.domain.model.aggregate.SysConfigAggregate;
import com.silverwing.biz.config.domain.model.query.ConfigQuery;
import com.silverwing.common.domain.PageResult;

import java.util.List;

/**
 * 参数配置仓储接口。
 */
public interface ConfigRepository {

    /** 分页查询 */
    PageResult<SysConfigAggregate> findPage(ConfigQuery query);

    /** 列表查询（用于导出） */
    List<SysConfigAggregate> findList(ConfigQuery query);

    /** 查询全部 */
    List<SysConfigAggregate> findAll();

    /** 根据主键查询 */
    SysConfigAggregate findById(Long id);

    /** 根据参数键名查询 */
    SysConfigAggregate findByConfigKey(String configKey);

    /** 保存（新增或更新） */
    void save(SysConfigAggregate aggregate);

    /** 根据主键删除 */
    void deleteById(Long id);

    /** 判断参数键名是否已存在（排除指定主键） */
    boolean existsByConfigKey(String configKey, Long excludeId);
}
