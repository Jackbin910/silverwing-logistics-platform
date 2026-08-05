package com.silverwing.admin.client.impl;

import com.silverwing.admin.application.dto.OperLogResponse;
import com.silverwing.admin.application.query.OperLogPageQuery;
import com.silverwing.admin.client.OperLogClient;
import com.silverwing.admin.client.convertor.OperLogConvertor;
import com.silverwing.biz.operlog.domain.adapter.repository.OperLogRepository;
import com.silverwing.biz.operlog.domain.model.aggregate.SysOperLogAggregate;
import com.silverwing.biz.operlog.domain.model.query.OperLogQuery;
import com.silverwing.common.domain.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 操作日志上下文防腐层适配器，串联领域仓储与应用对象。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OperLogClientImpl implements OperLogClient {

    private final OperLogConvertor operLogConvertor;
    private final OperLogRepository operLogRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResult<OperLogResponse> list(OperLogPageQuery query) {
        OperLogQuery domainQuery = toDomainQuery(query);
        PageResult<SysOperLogAggregate> page = operLogRepository.findPage(domainQuery);
        List<OperLogResponse> records = page.getRecords().stream()
                .map(operLogConvertor::toResponse)
                .collect(Collectors.toList());
        return new PageResult<>(page.getCurrent(), page.getSize(), page.getTotal(), records);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OperLogResponse> listExport(OperLogPageQuery query) {
        OperLogQuery domainQuery = toDomainQuery(query);
        return operLogRepository.findList(domainQuery).stream()
                .map(operLogConvertor::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public OperLogResponse getById(Long operId) {
        SysOperLogAggregate aggregate = operLogRepository.findById(operId);
        return aggregate == null ? null : operLogConvertor.toResponse(aggregate);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByIds(List<Long> operIds) {
        operLogRepository.deleteByIds(operIds);
        log.info("批量删除操作日志 数量={}", operIds == null ? 0 : operIds.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clean() {
        operLogRepository.clean();
        log.info("清空操作日志完成");
    }

    /** 将应用层分页查询翻译为操作日志领域查询 */
    private OperLogQuery toDomainQuery(OperLogPageQuery query) {
        OperLogQuery domainQuery = new OperLogQuery();
        domainQuery.setCurrent(query.getCurrent());
        domainQuery.setSize(query.getSize());
        domainQuery.setTitle(query.getTitle());
        domainQuery.setOperName(query.getOperName());
        domainQuery.setBusinessType(query.getBusinessType());
        domainQuery.setStatus(query.getStatus());
        domainQuery.setBeginTime(query.getBeginTime());
        domainQuery.setEndTime(query.getEndTime());
        return domainQuery;
    }
}
