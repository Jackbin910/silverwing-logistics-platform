package com.silverwing.biz.logininfor.infrastructure.adapter.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.silverwing.biz.logininfor.domain.adapter.repository.LogininforRepository;
import com.silverwing.biz.logininfor.domain.model.aggregate.SysLogininforAggregate;
import com.silverwing.biz.logininfor.domain.model.query.LogininforQuery;
import com.silverwing.biz.logininfor.infrastructure.adapter.repository.convertor.LogininforInfraConvertor;
import com.silverwing.biz.logininfor.infrastructure.dao.SysLogininforMapper;
import com.silverwing.biz.logininfor.infrastructure.dao.po.SysLogininforPO;
import com.silverwing.common.domain.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 系统访问记录仓储实现。
 * <p>基于 MyBatis-Plus 完成访问记录的查询、删除与清空，简单条件使用 wrapper，
 * 复杂查询通过 {@code SysLogininforMapper.xml} 承载。</p>
 *
 * @author silverwing
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class LogininforRepositoryImpl implements LogininforRepository {

    private final SysLogininforMapper logininforMapper;

    @Override
    public PageResult<SysLogininforAggregate> findPage(LogininforQuery query) {
        LambdaQueryWrapper<SysLogininforPO> wrapper = buildWrapper(query);
        wrapper.orderByDesc(SysLogininforPO::getAccessTime);
        Page<SysLogininforPO> page = new Page<>(query.getCurrent(), query.getSize());
        Page<SysLogininforPO> result = logininforMapper.selectPage(page, wrapper);
        List<SysLogininforAggregate> records = result.getRecords().stream()
                .map(LogininforInfraConvertor.INSTANCE::toDomain)
                .toList();
        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), records);
    }

    @Override
    public List<SysLogininforAggregate> findList(LogininforQuery query) {
        LambdaQueryWrapper<SysLogininforPO> wrapper = buildWrapper(query);
        wrapper.orderByDesc(SysLogininforPO::getAccessTime);
        return logininforMapper.selectList(wrapper).stream()
                .map(LogininforInfraConvertor.INSTANCE::toDomain)
                .toList();
    }

    @Override
    public void deleteByIds(List<Long> infoIds) {
        if (infoIds == null || infoIds.isEmpty()) {
            return;
        }
        logininforMapper.deleteBatchIds(infoIds);
    }

    @Override
    public void clean() {
        logininforMapper.delete(Wrappers.lambdaQuery());
    }

    /**
     * 构建查询条件（简单条件使用 wrapper，符合分层约定）。
     *
     * @param query 查询条件
     * @return MyBatis-Plus 条件构造器
     */
    private LambdaQueryWrapper<SysLogininforPO> buildWrapper(LogininforQuery query) {
        LambdaQueryWrapper<SysLogininforPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotBlank(query.getUserName()), SysLogininforPO::getUserName, query.getUserName());
        wrapper.like(StringUtils.isNotBlank(query.getIpaddr()), SysLogininforPO::getIpaddr, query.getIpaddr());
        wrapper.eq(query.getStatus() != null, SysLogininforPO::getStatus, query.getStatus());
        if (StringUtils.isNotBlank(query.getBeginTime()) && StringUtils.isNotBlank(query.getEndTime())) {
            wrapper.between(SysLogininforPO::getAccessTime, query.getBeginTime(), query.getEndTime());
        }
        return wrapper;
    }
}
