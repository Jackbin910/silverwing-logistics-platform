package com.silverwing.biz.config.domain.service.impl;

import com.silverwing.biz.config.domain.adapter.repository.ConfigRepository;
import com.silverwing.biz.config.domain.model.aggregate.SysConfigAggregate;
import com.silverwing.biz.config.domain.service.IConfigDomainService;
import com.silverwing.common.domain.ResultCode;
import com.silverwing.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 参数配置领域服务实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConfigDomainServiceImpl implements IConfigDomainService {

    private final ConfigRepository configRepository;

    @Override
    public void saveConfig(SysConfigAggregate aggregate) {
        if (configRepository.existsByConfigKey(aggregate.getConfigKey(), aggregate.getId())) {
            throw new BusinessException(ResultCode.DATA_ALREADY_EXISTS, "admin.config.key.exists", aggregate.getConfigKey());
        }
        configRepository.save(aggregate);
    }

    @Override
    public void deleteConfigById(Long id) {
        SysConfigAggregate aggregate = configRepository.findById(id);
        if (aggregate == null) {
            return;
        }
        if ("Y".equals(aggregate.getConfigType())) {
            throw new BusinessException(ResultCode.DATA_STATUS_ILLEGAL, "admin.config.builtin.not.delete", aggregate.getConfigKey());
        }
        configRepository.deleteById(id);
    }
}
