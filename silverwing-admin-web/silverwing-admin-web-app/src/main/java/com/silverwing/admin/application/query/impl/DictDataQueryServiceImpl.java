package com.silverwing.admin.application.query.impl;

import com.silverwing.admin.application.dto.DictDataResponse;
import com.silverwing.admin.application.query.DictDataPageQuery;
import com.silverwing.admin.application.query.DictDataQueryService;
import com.silverwing.admin.client.DictDataClient;
import com.silverwing.common.domain.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 字典数据查询服务实现。
 */
@Service
@RequiredArgsConstructor
public class DictDataQueryServiceImpl implements DictDataQueryService {

    private final DictDataClient dictDataClient;

    @Override
    public PageResult<DictDataResponse> list(DictDataPageQuery query) {
        return dictDataClient.list(query);
    }

    @Override
    public List<DictDataResponse> listExport(DictDataPageQuery query) {
        return dictDataClient.listExport(query);
    }

    @Override
    public DictDataResponse getById(Long id) {
        return dictDataClient.getById(id);
    }

    @Override
    public List<DictDataResponse> getByDictType(String dictType) {
        return dictDataClient.getByDictType(dictType);
    }
}
