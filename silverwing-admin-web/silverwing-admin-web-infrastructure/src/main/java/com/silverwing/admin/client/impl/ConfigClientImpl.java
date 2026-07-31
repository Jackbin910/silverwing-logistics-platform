package com.silverwing.admin.client.impl;

import com.silverwing.admin.application.command.SaveConfigCommand;
import com.silverwing.admin.application.dto.ConfigResponse;
import com.silverwing.admin.application.query.ConfigPageQuery;
import com.silverwing.admin.client.ConfigClient;
import com.silverwing.admin.client.convertor.ConfigConvertor;
import com.silverwing.biz.config.domain.adapter.repository.ConfigRepository;
import com.silverwing.biz.config.domain.model.aggregate.SysConfigAggregate;
import com.silverwing.biz.config.domain.model.query.ConfigQuery;
import com.silverwing.biz.config.domain.service.IConfigDomainService;
import com.silverwing.biz.config.infrastructure.cache.ConfigCache;
import com.silverwing.common.domain.PageResult;
import com.silverwing.common.domain.ResultCode;
import com.silverwing.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 参数配置上下文防腐层适配器。
 * <p>本类是唯一直接依赖 biz-config 参数配置领域层（聚合根、仓储、领域服务）的位置。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConfigClientImpl implements ConfigClient {

    private final ConfigRepository configRepository;
    private final ConfigConvertor configConvertor;
    private final IConfigDomainService configDomainService;
    private final ConfigCache configCache;

    @PostConstruct
    public void init() {
        try {
            loadConfigCache();
        } catch (Exception e) {
            log.warn("初始化参数配置缓存失败", e);
        }
    }

    @Override
    @Transactional
    public ConfigResponse create(SaveConfigCommand command) {
        SysConfigAggregate aggregate = configConvertor.toEntity(command);
        configDomainService.saveConfig(aggregate);
        configCache.put(aggregate.getConfigKey(), aggregate.getConfigValue());
        log.info("新建参数配置 configKey={}, id={}", aggregate.getConfigKey(), aggregate.getId());
        return configConvertor.toResponse(aggregate);
    }

    @Override
    @Transactional
    public void update(Long id, SaveConfigCommand command) {
        SysConfigAggregate aggregate = configRepository.findById(id);
        if (aggregate == null) {
            throw BusinessException.i18n(ResultCode.NOT_FOUND, "admin.config.notfound");
        }
        String oldKey = aggregate.getConfigKey();
        configConvertor.applyCommandToEntity(aggregate, command);
        configDomainService.saveConfig(aggregate);
        if (!oldKey.equals(aggregate.getConfigKey())) {
            configCache.remove(oldKey);
        }
        configCache.put(aggregate.getConfigKey(), aggregate.getConfigValue());
        log.info("更新参数配置 id={}", id);
    }

    @Override
    @Transactional
    public void deleteByIds(Long[] ids) {
        if (ids == null) {
            return;
        }
        for (Long id : ids) {
            SysConfigAggregate aggregate = configRepository.findById(id);
            if (aggregate == null) {
                continue;
            }
            configDomainService.deleteConfigById(id);
            configCache.remove(aggregate.getConfigKey());
        }
        log.info("批量删除参数配置 数量={}", ids.length);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<ConfigResponse> list(ConfigPageQuery query) {
        ConfigQuery domainQuery = toConfigQuery(query);
        PageResult<SysConfigAggregate> page = configRepository.findPage(domainQuery);
        List<ConfigResponse> records = page.getRecords().stream()
                .map(configConvertor::toResponse)
                .collect(Collectors.toList());
        return new PageResult<>(page.getCurrent(), page.getSize(), page.getTotal(), records);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConfigResponse> listExport(ConfigPageQuery query) {
        ConfigQuery domainQuery = toConfigQuery(query);
        return configRepository.findList(domainQuery).stream()
                .map(configConvertor::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ConfigResponse getById(Long id) {
        SysConfigAggregate aggregate = configRepository.findById(id);
        return aggregate == null ? null : configConvertor.toResponse(aggregate);
    }

    @Override
    @Transactional(readOnly = true)
    public String getByKey(String configKey) {
        String value = configCache.get(configKey);
        if (value != null) {
            return value;
        }
        SysConfigAggregate aggregate = configRepository.findByConfigKey(configKey);
        if (aggregate != null) {
            configCache.put(configKey, aggregate.getConfigValue());
            return aggregate.getConfigValue();
        }
        return null;
    }

    @Override
    public void refreshCache() {
        configCache.clear();
        loadConfigCache();
        log.info("刷新参数配置缓存完成");
    }

    /** 加载全部参数配置到缓存 */
    private void loadConfigCache() {
        List<SysConfigAggregate> all = configRepository.findAll();
        for (SysConfigAggregate aggregate : all) {
            configCache.put(aggregate.getConfigKey(), aggregate.getConfigValue());
        }
    }

    /** 将应用层分页查询翻译为参数配置领域查询 */
    private ConfigQuery toConfigQuery(ConfigPageQuery query) {
        ConfigQuery domainQuery = new ConfigQuery();
        domainQuery.setCurrent(query.getCurrent());
        domainQuery.setSize(query.getSize());
        domainQuery.setConfigName(query.getConfigName());
        domainQuery.setConfigKey(query.getConfigKey());
        domainQuery.setConfigType(query.getConfigType());
        return domainQuery;
    }
}
