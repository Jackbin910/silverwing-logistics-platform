package com.silverwing.biz.operlog.domain.adapter.repository;

import com.silverwing.biz.operlog.domain.model.aggregate.SysOperLogAggregate;
import com.silverwing.biz.operlog.domain.model.query.OperLogQuery;
import com.silverwing.common.domain.PageResult;

import java.util.List;

/**
 * 操作日志仓储接口。
 */
public interface OperLogRepository {

    /** 分页查询操作日志 */
    PageResult<SysOperLogAggregate> findPage(OperLogQuery query);

    /** 列表查询操作日志（不分页，用于导出） */
    List<SysOperLogAggregate> findList(OperLogQuery query);

    /** 根据ID查询操作日志 */
    SysOperLogAggregate findById(Long operId);

    /** 批量删除操作日志 */
    void deleteByIds(List<Long> operIds);

    /** 清空操作日志 */
    void clean();
}
