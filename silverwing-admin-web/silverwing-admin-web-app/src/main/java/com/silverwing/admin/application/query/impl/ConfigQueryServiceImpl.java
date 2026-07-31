package com.silverwing.admin.application.query.impl;

import com.silverwing.admin.application.dto.ConfigResponse;
import com.silverwing.admin.application.query.ConfigPageQuery;
import com.silverwing.admin.application.query.ConfigQueryService;
import com.silverwing.admin.client.ConfigClient;
import com.silverwing.common.domain.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 参数配置查询服务实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConfigQueryServiceImpl implements ConfigQueryService {

    private final ConfigClient configClient;

    @Override
    public PageResult<ConfigResponse> list(ConfigPageQuery query) {
        return configClient.list(query);
    }

    @Override
    public List<ConfigResponse> listExport(ConfigPageQuery query) {
        return configClient.listExport(query);
    }

    @Override
    public ConfigResponse getById(Long id) {
        return configClient.getById(id);
    }

    @Override
    public String getByKey(String configKey) {
        return configClient.getByKey(configKey);
    }
}
