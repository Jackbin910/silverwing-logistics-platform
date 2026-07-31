package com.silverwing.admin.application.query;

import com.silverwing.admin.application.dto.LogininforResponse;
import com.silverwing.common.domain.PageResult;

import java.util.List;

/**
 * 登录日志查询应用服务接口。
 */
public interface LogininforQueryService {

    /** 分页查询登录日志 */
    PageResult<LogininforResponse> list(LogininforPageQuery query);

    /** 查询导出数据（不分页） */
    List<LogininforResponse> listExport(LogininforPageQuery query);

    /** 根据ID查询登录日志 */
    LogininforResponse getById(Long infoId);
}
