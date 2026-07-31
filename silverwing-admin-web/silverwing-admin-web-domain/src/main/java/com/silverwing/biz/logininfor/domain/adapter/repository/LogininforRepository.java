package com.silverwing.biz.logininfor.domain.adapter.repository;

import com.silverwing.biz.logininfor.domain.model.aggregate.SysLogininforAggregate;
import com.silverwing.biz.logininfor.domain.model.query.LogininforQuery;
import com.silverwing.common.domain.PageResult;

import java.util.List;

/**
 * 系统访问记录仓储接口。
 */
public interface LogininforRepository {

    /** 分页查询登录日志 */
    PageResult<SysLogininforAggregate> findPage(LogininforQuery query);

    /** 列表查询（用于导出） */
    List<SysLogininforAggregate> findList(LogininforQuery query);

    /** 根据主键查询 */
    SysLogininforAggregate findById(Long infoId);

    /** 新增登录日志 */
    void insert(SysLogininforAggregate aggregate);

    /** 批量删除（物理删除） */
    void deleteByIds(List<Long> infoIds);

    /** 清空全部登录日志（物理删除） */
    void clean();
}
