package com.silverwing.admin.application.query.impl;

import com.silverwing.admin.application.dto.OperLogResponse;
import com.silverwing.admin.application.query.OperLogPageQuery;
import com.silverwing.admin.application.query.OperLogQueryService;
import com.silverwing.admin.client.OperLogClient;
import com.silverwing.common.domain.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 操作日志查询应用服务实现。
 */
@Service
@RequiredArgsConstructor
public class OperLogQueryServiceImpl implements OperLogQueryService {

    private final OperLogClient operLogClient;

    @Override
    public PageResult<OperLogResponse> list(OperLogPageQuery query) {
        return operLogClient.list(query);
    }

    @Override
    public List<OperLogResponse> listExport(OperLogPageQuery query) {
        return operLogClient.listExport(query);
    }

    @Override
    public OperLogResponse getById(Long operId) {
        return operLogClient.getById(operId);
    }
}
