package com.silverwing.biz.logininfor.domain.service.impl;

import com.silverwing.biz.logininfor.domain.adapter.repository.LogininforRepository;
import com.silverwing.biz.logininfor.domain.model.aggregate.SysLogininforAggregate;
import com.silverwing.biz.logininfor.domain.model.query.LogininforQuery;
import com.silverwing.biz.logininfor.domain.service.ILogininforDomainService;
import com.silverwing.common.domain.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 系统访问记录领域服务实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LogininforDomainServiceImpl implements ILogininforDomainService {

    private final LogininforRepository logininforRepository;

    @Override
    public PageResult<SysLogininforAggregate> pageLogininfor(LogininforQuery query) {
        return logininforRepository.findPage(query);
    }

    @Override
    public List<SysLogininforAggregate> listLogininfor(LogininforQuery query) {
        return logininforRepository.findList(query);
    }

    @Override
    public void insertLogininfor(SysLogininforAggregate aggregate) {
        logininforRepository.insert(aggregate);
    }

    @Override
    public void deleteLogininforByIds(List<Long> infoIds) {
        logininforRepository.deleteByIds(infoIds);
    }

    @Override
    public void cleanLogininfor() {
        logininforRepository.clean();
    }

    @Override
    public SysLogininforAggregate getLogininforById(Long infoId) {
        return logininforRepository.findById(infoId);
    }
}
