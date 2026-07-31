package com.silverwing.admin.application.query.impl;

import com.silverwing.admin.application.dto.DictTypeResponse;
import com.silverwing.admin.application.query.DictTypePageQuery;
import com.silverwing.admin.application.query.DictTypeQueryService;
import com.silverwing.admin.client.DictTypeClient;
import com.silverwing.common.domain.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 字典类型查询服务实现。
 */
@Service
@RequiredArgsConstructor
public class DictTypeQueryServiceImpl implements DictTypeQueryService {

    private final DictTypeClient dictTypeClient;

    @Override
    public PageResult<DictTypeResponse> list(DictTypePageQuery query) {
        return dictTypeClient.list(query);
    }

    @Override
    public List<DictTypeResponse> listExport(DictTypePageQuery query) {
        return dictTypeClient.listExport(query);
    }

    @Override
    public DictTypeResponse getById(Long id) {
        return dictTypeClient.getById(id);
    }

    @Override
    public List<DictTypeResponse> optionSelect() {
        return dictTypeClient.optionSelect();
    }

    @Override
    public void refreshCache() {
        dictTypeClient.refreshCache();
    }
}
