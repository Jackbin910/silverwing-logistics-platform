package com.silverwing.admin.client;

import com.silverwing.admin.application.dto.OperLogResponse;
import com.silverwing.admin.application.query.OperLogPageQuery;
import com.silverwing.common.domain.PageResult;

import java.util.List;

/**
 * 操作日志上下文防腐层接口。
 */
public interface OperLogClient {

    /** 分页查询操作日志 */
    PageResult<OperLogResponse> list(OperLogPageQuery query);

    /** 导出查询（不分页） */
    List<OperLogResponse> listExport(OperLogPageQuery query);

    /** 根据ID查询 */
    OperLogResponse getById(Long operId);

    /** 批量删除操作日志 */
    void removeByIds(List<Long> operIds);

    /** 清空操作日志 */
    void clean();
}
