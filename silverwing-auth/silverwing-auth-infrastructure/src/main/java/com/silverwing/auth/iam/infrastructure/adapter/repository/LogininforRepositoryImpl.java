package com.silverwing.auth.iam.infrastructure.adapter.repository;

import com.silverwing.auth.iam.domain.adapter.repository.LogininforRepository;
import com.silverwing.auth.iam.domain.model.aggregate.LogininforAggregate;
import com.silverwing.auth.iam.infrastructure.adapter.repository.convertor.LogininforInfraConvertor;
import com.silverwing.auth.iam.infrastructure.dao.SysLogininforDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * 系统访问记录仓储实现。
 * <p>由认证流程写入登录/访问日志，落库至 {@code sys_logininfor} 表。</p>
 *
 * @author silverwing
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class LogininforRepositoryImpl implements LogininforRepository {

    private final SysLogininforDao logininforDao;

    @Override
    public void insert(LogininforAggregate aggregate) {
        logininforDao.insert(LogininforInfraConvertor.INSTANCE.toPo(aggregate));
    }
}
