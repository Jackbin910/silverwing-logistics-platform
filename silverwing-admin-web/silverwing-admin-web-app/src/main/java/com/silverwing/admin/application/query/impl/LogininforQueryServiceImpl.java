package com.silverwing.admin.application.query.impl;

import com.silverwing.admin.application.dto.LogininforResponse;
import com.silverwing.admin.application.query.LogininforPageQuery;
import com.silverwing.admin.application.query.LogininforQueryService;
import com.silverwing.admin.client.LogininforClient;
import com.silverwing.common.domain.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 系统访问记录查询服务实现。
 *
 * @author silverwing
 */
@Service
@RequiredArgsConstructor
public class LogininforQueryServiceImpl implements LogininforQueryService {

    private final LogininforClient logininforClient;

    @Override
    public PageResult<LogininforResponse> list(LogininforPageQuery query) {
        return logininforClient.list(query);
    }

    @Override
    public List<LogininforResponse> listExport(LogininforPageQuery query) {
        return logininforClient.listExport(query);
    }
}
