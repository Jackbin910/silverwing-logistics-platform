package com.silverwing.biz.logininfor.domain.adapter.repository;

import com.silverwing.biz.logininfor.domain.model.aggregate.SysLogininforAggregate;
import com.silverwing.biz.logininfor.domain.model.query.LogininforQuery;
import com.silverwing.common.domain.PageResult;

import java.util.List;

/**
 * 系统访问记录仓储端口。
 * <p>定义访问记录的查询、删除与清空能力，由基础设施层实现。</p>
 *
 * @author silverwing
 */
public interface LogininforRepository {

    /**
     * 分页查询访问记录。
     *
     * @param query 查询条件（含分页）
     * @return 分页结果（聚合根）
     */
    PageResult<SysLogininforAggregate> findPage(LogininforQuery query);

    /**
     * 列表查询访问记录（不分页，用于导出）。
     *
     * @param query 查询条件
     * @return 聚合根列表
     */
    List<SysLogininforAggregate> findList(LogininforQuery query);

    /**
     * 根据访问ID批量删除访问记录。
     *
     * @param infoIds 访问ID列表
     */
    void deleteByIds(List<Long> infoIds);

    /**
     * 清空全部访问记录。
     */
    void clean();
}
