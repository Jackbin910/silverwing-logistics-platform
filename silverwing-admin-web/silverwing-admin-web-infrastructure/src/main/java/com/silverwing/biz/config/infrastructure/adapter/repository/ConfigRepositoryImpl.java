package com.silverwing.biz.config.infrastructure.adapter.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.silverwing.biz.config.domain.adapter.repository.ConfigRepository;
import com.silverwing.biz.config.domain.model.aggregate.SysConfigAggregate;
import com.silverwing.biz.config.domain.model.query.ConfigQuery;
import com.silverwing.biz.config.infrastructure.adapter.repository.convertor.ConfigInfraConvertor;
import com.silverwing.biz.config.infrastructure.dao.SysConfigDao;
import com.silverwing.biz.config.infrastructure.dao.po.SysConfigPO;
import com.silverwing.common.domain.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 参数配置仓储实现。
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class ConfigRepositoryImpl implements ConfigRepository {

    private final SysConfigDao sysConfigDao;

    @Override
    public PageResult<SysConfigAggregate> findPage(ConfigQuery query) {
        LambdaQueryWrapper<SysConfigPO> wrapper = buildWrapper(query);
        wrapper.orderByDesc(SysConfigPO::getId);
        Page<SysConfigPO> page = new Page<>(query.getCurrent(), query.getSize());
        Page<SysConfigPO> result = sysConfigDao.selectPage(page, wrapper);
        List<SysConfigAggregate> records = result.getRecords().stream()
                .map(ConfigInfraConvertor.INSTANCE::toDomain)
                .toList();
        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), records);
    }

    @Override
    public List<SysConfigAggregate> findList(ConfigQuery query) {
        LambdaQueryWrapper<SysConfigPO> wrapper = buildWrapper(query);
        wrapper.orderByDesc(SysConfigPO::getId);
        return sysConfigDao.selectList(wrapper).stream()
                .map(ConfigInfraConvertor.INSTANCE::toDomain)
                .toList();
    }

    @Override
    public List<SysConfigAggregate> findAll() {
        LambdaQueryWrapper<SysConfigPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(SysConfigPO::getId);
        return sysConfigDao.selectList(wrapper).stream()
                .map(ConfigInfraConvertor.INSTANCE::toDomain)
                .toList();
    }

    @Override
    public SysConfigAggregate findById(Long id) {
        SysConfigPO po = sysConfigDao.selectById(id);
        return po == null ? null : ConfigInfraConvertor.INSTANCE.toDomain(po);
    }

    @Override
    public SysConfigAggregate findByConfigKey(String configKey) {
        LambdaQueryWrapper<SysConfigPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfigPO::getConfigKey, configKey);
        wrapper.last("limit 1");
        SysConfigPO po = sysConfigDao.selectOne(wrapper);
        return po == null ? null : ConfigInfraConvertor.INSTANCE.toDomain(po);
    }

    @Override
    public void save(SysConfigAggregate aggregate) {
        SysConfigPO po = ConfigInfraConvertor.INSTANCE.toPo(aggregate);
        if (aggregate.getId() == null) {
            sysConfigDao.insert(po);
            aggregate.setId(po.getId());
        } else {
            sysConfigDao.updateById(po);
        }
    }

    @Override
    public void deleteById(Long id) {
        sysConfigDao.deleteById(id);
    }

    @Override
    public boolean existsByConfigKey(String configKey, Long excludeId) {
        LambdaQueryWrapper<SysConfigPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfigPO::getConfigKey, configKey);
        if (excludeId != null) {
            wrapper.ne(SysConfigPO::getId, excludeId);
        }
        return sysConfigDao.selectCount(wrapper) > 0;
    }

    /** 构建查询条件 */
    private LambdaQueryWrapper<SysConfigPO> buildWrapper(ConfigQuery query) {
        LambdaQueryWrapper<SysConfigPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotBlank(query.getConfigName()), SysConfigPO::getConfigName, query.getConfigName());
        wrapper.like(StringUtils.isNotBlank(query.getConfigKey()), SysConfigPO::getConfigKey, query.getConfigKey());
        wrapper.eq(StringUtils.isNotBlank(query.getConfigType()), SysConfigPO::getConfigType, query.getConfigType());
        return wrapper;
    }
}
