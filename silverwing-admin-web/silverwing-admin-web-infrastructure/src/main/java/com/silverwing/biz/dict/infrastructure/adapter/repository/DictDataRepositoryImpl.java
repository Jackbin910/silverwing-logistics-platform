package com.silverwing.biz.dict.infrastructure.adapter.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.silverwing.biz.dict.domain.adapter.repository.DictDataRepository;
import com.silverwing.biz.dict.domain.model.aggregate.SysDictDataAggregate;
import com.silverwing.biz.dict.domain.model.query.DictDataQuery;
import com.silverwing.biz.dict.infrastructure.adapter.repository.convertor.DictDataInfraConvertor;
import com.silverwing.biz.dict.infrastructure.dao.SysDictDataDao;
import com.silverwing.biz.dict.infrastructure.dao.po.SysDictDataPO;
import com.silverwing.common.domain.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 字典数据仓储实现（基础设施适配器）。
 */
@Repository
@RequiredArgsConstructor
public class DictDataRepositoryImpl implements DictDataRepository {

    private final SysDictDataDao dictDataDao;

    @Override
    public SysDictDataAggregate findById(Long dictCode) {
        SysDictDataPO po = dictDataDao.selectById(dictCode);
        return po == null ? null : DictDataInfraConvertor.INSTANCE.toDomain(po);
    }

    @Override
    public PageResult<SysDictDataAggregate> findPage(DictDataQuery query) {
        query.normalize();
        Page<SysDictDataPO> pageObj = new Page<>(query.getCurrent(), query.getSize());
        LambdaQueryWrapper<SysDictDataPO> wrapper = new LambdaQueryWrapper<>();
        if (query.getDictLabel() != null && !query.getDictLabel().isBlank()) {
            wrapper.like(SysDictDataPO::getDictLabel, query.getDictLabel());
        }
        if (query.getDictValue() != null && !query.getDictValue().isBlank()) {
            wrapper.like(SysDictDataPO::getDictValue, query.getDictValue());
        }
        if (query.getDictType() != null && !query.getDictType().isBlank()) {
            wrapper.eq(SysDictDataPO::getDictType, query.getDictType());
        }
        if (query.getStatus() != null && !query.getStatus().isBlank()) {
            wrapper.eq(SysDictDataPO::getStatus, query.getStatus());
        }
        wrapper.orderByAsc(SysDictDataPO::getDictSort)
                .orderByAsc(SysDictDataPO::getId);
        Page<SysDictDataPO> result = dictDataDao.selectPage(pageObj, wrapper);
        List<SysDictDataAggregate> records = result.getRecords().stream()
                .map(DictDataInfraConvertor.INSTANCE::toDomain)
                .toList();
        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), records);
    }

    @Override
    public List<SysDictDataAggregate> findList(DictDataQuery query) {
        LambdaQueryWrapper<SysDictDataPO> wrapper = new LambdaQueryWrapper<>();
        if (query.getDictLabel() != null && !query.getDictLabel().isBlank()) {
            wrapper.like(SysDictDataPO::getDictLabel, query.getDictLabel());
        }
        if (query.getDictValue() != null && !query.getDictValue().isBlank()) {
            wrapper.like(SysDictDataPO::getDictValue, query.getDictValue());
        }
        if (query.getDictType() != null && !query.getDictType().isBlank()) {
            wrapper.eq(SysDictDataPO::getDictType, query.getDictType());
        }
        if (query.getStatus() != null && !query.getStatus().isBlank()) {
            wrapper.eq(SysDictDataPO::getStatus, query.getStatus());
        }
        wrapper.orderByAsc(SysDictDataPO::getDictSort)
                .orderByAsc(SysDictDataPO::getId);
        return dictDataDao.selectList(wrapper).stream().map(DictDataInfraConvertor.INSTANCE::toDomain).toList();
    }

    @Override
    public List<SysDictDataAggregate> findByDictType(String dictType) {
        LambdaQueryWrapper<SysDictDataPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDictDataPO::getDictType, dictType);
        wrapper.orderByAsc(SysDictDataPO::getDictSort)
                .orderByAsc(SysDictDataPO::getId);
        return dictDataDao.selectList(wrapper).stream().map(DictDataInfraConvertor.INSTANCE::toDomain).toList();
    }

    @Override
    public void save(SysDictDataAggregate aggregate) {
        SysDictDataPO po = DictDataInfraConvertor.INSTANCE.toPo(aggregate);
        if (aggregate.getId() != null) {
            dictDataDao.updateById(po);
        } else {
            dictDataDao.insert(po);
            aggregate.setId(po.getId());
        }
    }

    @Override
    public void deleteById(Long dictCode) {
        dictDataDao.deleteById(dictCode);
    }

    @Override
    public boolean existsByDictTypeAndValue(String dictType, String dictValue, Long excludeId) {
        LambdaQueryWrapper<SysDictDataPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDictDataPO::getDictType, dictType);
        wrapper.eq(SysDictDataPO::getDictValue, dictValue);
        if (excludeId != null) {
            wrapper.ne(SysDictDataPO::getId, excludeId);
        }
        return dictDataDao.selectCount(wrapper) > 0;
    }
}
