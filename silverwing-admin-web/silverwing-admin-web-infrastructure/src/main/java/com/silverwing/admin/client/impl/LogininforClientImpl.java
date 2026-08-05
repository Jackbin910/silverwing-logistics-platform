package com.silverwing.admin.client.impl;

import com.silverwing.admin.application.dto.LogininforResponse;
import com.silverwing.admin.application.query.LogininforPageQuery;
import com.silverwing.admin.client.LogininforClient;
import com.silverwing.admin.client.convertor.LogininforConvertor;
import com.silverwing.biz.logininfor.domain.adapter.repository.LogininforRepository;
import com.silverwing.biz.logininfor.domain.model.aggregate.SysLogininforAggregate;
import com.silverwing.biz.logininfor.domain.model.query.LogininforQuery;
import com.silverwing.common.domain.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统访问记录防腐层适配器，串联领域仓储与应用对象。
 *
 * @author silverwing
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LogininforClientImpl implements LogininforClient {

    private final LogininforConvertor logininforConvertor;

    private final LogininforRepository logininforRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResult<LogininforResponse> list(LogininforPageQuery query) {
        LogininforQuery domainQuery = toDomainQuery(query);
        PageResult<SysLogininforAggregate> page = logininforRepository.findPage(domainQuery);
        List<LogininforResponse> records = page.getRecords().stream()
                .map(logininforConvertor::toResponse)
                .collect(Collectors.toList());
        return new PageResult<>(page.getCurrent(), page.getSize(), page.getTotal(), records);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LogininforResponse> listExport(LogininforPageQuery query) {
        LogininforQuery domainQuery = toDomainQuery(query);
        return logininforRepository.findList(domainQuery).stream()
                .map(logininforConvertor::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByIds(Long[] infoIds) {
        logininforRepository.deleteByIds(List.of(infoIds));
        log.info("批量删除登录日志 数量={}", infoIds == null ? 0 : infoIds.length);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clean() {
        logininforRepository.clean();
        log.info("清空登录日志完成");
    }

    /**
     * 将应用层分页查询翻译为访问记录领域查询。
     *
     * @param query 应用层查询条件
     * @return 领域查询条件
     */
    private LogininforQuery toDomainQuery(LogininforPageQuery query) {
        LogininforQuery domainQuery = new LogininforQuery();
        domainQuery.setCurrent(query.getCurrent());
        domainQuery.setSize(query.getSize());
        domainQuery.setUserName(query.getUserName());
        domainQuery.setIpaddr(query.getIpaddr());
        domainQuery.setStatus(query.getStatus());
        domainQuery.setBeginTime(query.getBeginTime());
        domainQuery.setEndTime(query.getEndTime());
        return domainQuery;
    }
}
