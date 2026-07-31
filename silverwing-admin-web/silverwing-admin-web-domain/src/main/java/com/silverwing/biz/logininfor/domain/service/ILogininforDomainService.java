package com.silverwing.biz.logininfor.domain.service;

import com.silverwing.biz.logininfor.domain.model.aggregate.SysLogininforAggregate;
import com.silverwing.biz.logininfor.domain.model.query.LogininforQuery;
import com.silverwing.common.domain.PageResult;

import java.util.List;

/**
 * 系统访问记录领域服务接口。
 */
public interface ILogininforDomainService {

    /** 分页查询登录日志 */
    PageResult<SysLogininforAggregate> pageLogininfor(LogininforQuery query);

    /** 列表查询登录日志（不分页，用于导出） */
    List<SysLogininforAggregate> listLogininfor(LogininforQuery query);

    /** 新增登录日志 */
    void insertLogininfor(SysLogininforAggregate aggregate);

    /** 批量删除登录日志 */
    void deleteLogininforByIds(List<Long> infoIds);

    /** 清空登录日志 */
    void cleanLogininfor();

    /** 根据ID查询登录日志 */
    SysLogininforAggregate getLogininforById(Long infoId);
}
