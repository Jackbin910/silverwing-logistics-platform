package com.silverwing.admin.application.query;

import com.silverwing.admin.application.dto.OperLogResponse;
import com.silverwing.common.domain.PageResult;

import java.util.List;

/**
 * 操作日志查询应用服务接口。
 */
public interface OperLogQueryService {

    /** 分页查询操作日志 */
    PageResult<OperLogResponse> list(OperLogPageQuery query);

    /** 查询导出数据（不分页） */
    List<OperLogResponse> listExport(OperLogPageQuery query);

    /** 根据ID查询操作日志 */
    OperLogResponse getById(Long operId);
}
