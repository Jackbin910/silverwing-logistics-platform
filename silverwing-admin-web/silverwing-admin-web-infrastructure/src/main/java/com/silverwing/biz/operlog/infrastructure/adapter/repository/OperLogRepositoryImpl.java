package com.silverwing.biz.operlog.infrastructure.adapter.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.silverwing.biz.iam.infrastructure.dao.SysOperLogMapper;
import com.silverwing.biz.iam.infrastructure.dao.po.SysOperLogPO;
import com.silverwing.biz.operlog.domain.adapter.repository.OperLogRepository;
import com.silverwing.biz.operlog.domain.model.aggregate.SysOperLogAggregate;
import com.silverwing.biz.operlog.domain.model.query.OperLogQuery;
import com.silverwing.biz.operlog.infrastructure.adapter.repository.convertor.OperLogInfraConvertor;
import com.silverwing.common.domain.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 操作日志仓储实现。
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class OperLogRepositoryImpl implements OperLogRepository {

    private final SysOperLogMapper operLogMapper;

    @Override
    public PageResult<SysOperLogAggregate> findPage(OperLogQuery query) {
        LambdaQueryWrapper<SysOperLogPO> wrapper = buildWrapper(query);
        wrapper.orderByDesc(SysOperLogPO::getOperTime);
        Page<SysOperLogPO> page = new Page<>(query.getCurrent(), query.getSize());
        Page<SysOperLogPO> result = operLogMapper.selectPage(page, wrapper);
        List<SysOperLogAggregate> records = result.getRecords().stream()
                .map(OperLogInfraConvertor.INSTANCE::toDomain)
                .toList();
        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), records);
    }

    @Override
    public List<SysOperLogAggregate> findList(OperLogQuery query) {
        LambdaQueryWrapper<SysOperLogPO> wrapper = buildWrapper(query);
        wrapper.orderByDesc(SysOperLogPO::getOperTime);
        return operLogMapper.selectList(wrapper).stream()
                .map(OperLogInfraConvertor.INSTANCE::toDomain)
                .toList();
    }

    @Override
    public SysOperLogAggregate findById(Long operId) {
        SysOperLogPO po = operLogMapper.selectById(operId);
        return po == null ? null : OperLogInfraConvertor.INSTANCE.toDomain(po);
    }

    @Override
    public void deleteByIds(List<Long> operIds) {
        if (operIds == null || operIds.isEmpty()) {
            return;
        }
        operLogMapper.deleteBatchIds(operIds);
    }

    @Override
    public void clean() {
        operLogMapper.delete(Wrappers.lambdaQuery());
    }

    /** 构建查询条件（简单条件使用 wrapper，符合分层约定） */
    private LambdaQueryWrapper<SysOperLogPO> buildWrapper(OperLogQuery query) {
        LambdaQueryWrapper<SysOperLogPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotBlank(query.getTitle()), SysOperLogPO::getTitle, query.getTitle());
        wrapper.like(StringUtils.isNotBlank(query.getOperName()), SysOperLogPO::getOperName, query.getOperName());
        wrapper.eq(query.getBusinessType() != null, SysOperLogPO::getBusinessType, query.getBusinessType());
        wrapper.eq(query.getStatus() != null, SysOperLogPO::getStatus, query.getStatus());
        if (StringUtils.isNotBlank(query.getBeginTime()) && StringUtils.isNotBlank(query.getEndTime())) {
            wrapper.between(SysOperLogPO::getOperTime, query.getBeginTime(), query.getEndTime());
        }
        return wrapper;
    }
}
