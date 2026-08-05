package com.silverwing.auth.iam.domain.adapter.repository;

import com.silverwing.auth.iam.domain.model.aggregate.LogininforAggregate;

/**
 * 系统访问记录仓储端口（领域契约）。
 * <p>定义登录日志的写入与清空能力，由基础设施层实现。</p>
 *
 * @author silverwing
 */
public interface LogininforRepository {

    /**
     * 写入一条登录/访问日志。
     *
     * @param aggregate 登录日志聚合根
     */
    void insert(LogininforAggregate aggregate);
}
