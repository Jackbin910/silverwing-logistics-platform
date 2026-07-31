package com.silverwing.admin.client.impl;

import com.silverwing.admin.application.command.SaveLogininforCommand;
import com.silverwing.admin.application.dto.LogininforResponse;
import com.silverwing.admin.application.query.LogininforPageQuery;
import com.silverwing.admin.client.LogininforClient;
import com.silverwing.admin.client.convertor.LogininforConvertor;
import com.silverwing.biz.logininfor.domain.model.aggregate.SysLogininforAggregate;
import com.silverwing.biz.logininfor.domain.model.query.LogininforQuery;
import com.silverwing.biz.logininfor.domain.service.ILogininforDomainService;
import com.silverwing.common.domain.PageResult;
import com.silverwing.common.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 登录日志上下文防腐层适配器，串联领域服务与仓储。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LogininforClientImpl implements LogininforClient {

    /** 登录密码错误计数缓存前缀（与认证模块保持一致） */
    private static final String PWD_ERR_CNT_KEY = "pwd_err_cnt:";

    private final LogininforConvertor logininforConvertor;
    private final ILogininforDomainService logininforDomainService;
    private final RedisUtil redisUtil;

    @Override
    @Transactional(readOnly = true)
    public PageResult<LogininforResponse> list(LogininforPageQuery query) {
        LogininforQuery domainQuery = toDomainQuery(query);
        PageResult<SysLogininforAggregate> page = logininforDomainService.pageLogininfor(domainQuery);
        List<LogininforResponse> records = page.getRecords().stream()
                .map(logininforConvertor::toResponse)
                .collect(Collectors.toList());
        return new PageResult<>(page.getCurrent(), page.getSize(), page.getTotal(), records);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LogininforResponse> listExport(LogininforPageQuery query) {
        LogininforQuery domainQuery = toDomainQuery(query);
        return logininforDomainService.listLogininfor(domainQuery).stream()
                .map(logininforConvertor::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public LogininforResponse getById(Long infoId) {
        SysLogininforAggregate aggregate = logininforDomainService.getLogininforById(infoId);
        return aggregate == null ? null : logininforConvertor.toResponse(aggregate);
    }

    @Override
    @Transactional
    public void add(SaveLogininforCommand command) {
        SysLogininforAggregate aggregate = logininforConvertor.toEntity(command);
        logininforDomainService.insertLogininfor(aggregate);
        log.info("记录登录日志 userName={}, status={}", aggregate.getUserName(), aggregate.getStatus());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByIds(List<Long> infoIds) {
        logininforDomainService.deleteLogininforByIds(infoIds);
        log.info("批量删除登录日志 数量={}", infoIds == null ? 0 : infoIds.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clean() {
        logininforDomainService.cleanLogininfor();
        log.info("清空登录日志完成");
    }

    @Override
    public void unlock(String userName) {
        redisUtil.delete(PWD_ERR_CNT_KEY + userName);
        log.info("解锁用户账户 userName={}", userName);
    }

    /** 将应用层分页查询翻译为登录日志领域查询 */
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
