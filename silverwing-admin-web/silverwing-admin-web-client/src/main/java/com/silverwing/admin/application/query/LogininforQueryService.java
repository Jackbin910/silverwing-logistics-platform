package com.silverwing.admin.application.query;

import com.silverwing.admin.application.dto.LogininforResponse;
import com.silverwing.common.domain.PageResult;

import java.util.List;

/**
 * 系统访问记录查询服务端口。
 * <p>定义访问记录的分页查询与导出查询能力，由基础设施层实现。</p>
 *
 * @author silverwing
 */
public interface LogininforQueryService {

    /**
     * 分页查询访问记录。
     *
     * @param query 分页查询条件
     * @return 分页结果
     */
    PageResult<LogininforResponse> list(LogininforPageQuery query);

    /**
     * 查询访问记录用于导出（不分页）。
     *
     * @param query 查询条件
     * @return 访问记录列表
     */
    List<LogininforResponse> listExport(LogininforPageQuery query);
}
