package com.silverwing.biz.logininfor.infrastructure.adapter.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.silverwing.biz.logininfor.domain.adapter.repository.LogininforRepository;
import com.silverwing.biz.logininfor.domain.model.aggregate.SysLogininforAggregate;
import com.silverwing.biz.logininfor.domain.model.query.LogininforQuery;
import com.silverwing.biz.logininfor.infrastructure.adapter.repository.convertor.LogininforInfraConvertor;
import com.silverwing.biz.logininfor.infrastructure.dao.SysLogininforDao;
import com.silverwing.biz.logininfor.infrastructure.dao.po.SysLogininforPO;
import com.silverwing.common.domain.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * 系统访问记录仓储实现。
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class LogininforRepositoryImpl implements LogininforRepository {

    private final SysLogininforDao logininforDao;

    @Override
    public PageResult<SysLogininforAggregate> findPage(LogininforQuery query) {
        LambdaQueryWrapper<SysLogininforPO> wrapper = buildWrapper(query);
        wrapper.orderByDesc(SysLogininforPO::getInfoId);
        Page<SysLogininforPO> page = new Page<>(query.getCurrent(), query.getSize());
        Page<SysLogininforPO> result = logininforDao.selectPage(page, wrapper);
        List<SysLogininforAggregate> records = result.getRecords().stream()
                .map(LogininforInfraConvertor.INSTANCE::toDomain)
                .toList();
        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), records);
    }

    @Override
    public List<SysLogininforAggregate> findList(LogininforQuery query) {
        LambdaQueryWrapper<SysLogininforPO> wrapper = buildWrapper(query);
        wrapper.orderByDesc(SysLogininforPO::getInfoId);
        return logininforDao.selectList(wrapper).stream()
                .map(LogininforInfraConvertor.INSTANCE::toDomain)
                .toList();
    }

    @Override
    public SysLogininforAggregate findById(Long infoId) {
        SysLogininforPO po = logininforDao.selectById(infoId);
        return po == null ? null : LogininforInfraConvertor.INSTANCE.toDomain(po);
    }

    @Override
    public void insert(SysLogininforAggregate aggregate) {
        // 访问时间由服务落库时填充，与 RuoYi 中 access_time 使用 sysdate() 的行为一致
        if (aggregate.getAccessTime() == null) {
            aggregate.setAccessTime(new Date());
        }
        logininforDao.insert(LogininforInfraConvertor.INSTANCE.toPo(aggregate));
    }

    @Override
    public void deleteByIds(List<Long> infoIds) {
        if (infoIds == null || infoIds.isEmpty()) {
            return;
        }
        logininforDao.deleteBatchIds(infoIds);
    }

    @Override
    public void clean() {
        logininforDao.cleanLogininfor();
    }

    /** 构建查询条件 */
    private LambdaQueryWrapper<SysLogininforPO> buildWrapper(LogininforQuery query) {
        LambdaQueryWrapper<SysLogininforPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotBlank(query.getUserName()), SysLogininforPO::getUserName, query.getUserName());
        wrapper.like(StringUtils.isNotBlank(query.getIpaddr()), SysLogininforPO::getIpaddr, query.getIpaddr());
        wrapper.eq(StringUtils.isNotBlank(query.getStatus()), SysLogininforPO::getStatus, query.getStatus());
        if (StringUtils.isNotBlank(query.getBeginTime()) && StringUtils.isNotBlank(query.getEndTime())) {
            wrapper.between(SysLogininforPO::getAccessTime, query.getBeginTime(), query.getEndTime());
        }
        return wrapper;
    }
}
