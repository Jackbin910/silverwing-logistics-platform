package com.silverwing.biz.config.domain.service;

import com.silverwing.biz.config.domain.model.aggregate.SysConfigAggregate;

/**
 * 参数配置领域服务。
 */
public interface IConfigDomainService {

    /** 保存参数配置（含参数键名唯一性校验） */
    void saveConfig(SysConfigAggregate aggregate);

    /** 根据主键删除参数配置（内置参数不允许删除） */
    void deleteConfigById(Long id);
}
