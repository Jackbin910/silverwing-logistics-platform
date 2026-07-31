package com.silverwing.biz.dict.infrastructure.adapter.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.silverwing.biz.dict.domain.adapter.repository.DictTypeRepository;
import com.silverwing.biz.dict.domain.model.aggregate.SysDictTypeAggregate;
import com.silverwing.biz.dict.domain.model.query.DictTypeQuery;
import com.silverwing.biz.dict.infrastructure.adapter.repository.convertor.DictTypeInfraConvertor;
import com.silverwing.biz.dict.infrastructure.dao.SysDictTypeDao;
import com.silverwing.biz.dict.infrastructure.dao.po.SysDictTypePO;
import com.silverwing.common.domain.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 字典类型仓储实现（基础设施适配器）。
 */
@Repository
@RequiredArgsConstructor
public class DictTypeRepositoryImpl implements DictTypeRepository {

    private final SysDictTypeDao dictTypeDao;

    @Override
    public SysDictTypeAggregate findById(Long dictId) {
        SysDictTypePO po = dictTypeDao.selectById(dictId);
        return po == null ? null : DictTypeInfraConvertor.INSTANCE.toDomain(po);
    }

    @Override
    public PageResult<SysDictTypeAggregate> findPage(DictTypeQuery query) {
        query.normalize();
        Page<SysDictTypePO> pageObj = new Page<>(query.getCurrent(), query.getSize());
        LambdaQueryWrapper<SysDictTypePO> wrapper = new LambdaQueryWrapper<>();
        if (query.getDictName() != null && !query.getDictName().isBlank()) {
            wrapper.like(SysDictTypePO::getDictName, query.getDictName());
        }
        if (query.getDictType() != null && !query.getDictType().isBlank()) {
            wrapper.like(SysDictTypePO::getDictType, query.getDictType());
        }
        if (query.getStatus() != null && !query.getStatus().isBlank()) {
            wrapper.eq(SysDictTypePO::getStatus, query.getStatus());
        }
        wrapper.orderByAsc(SysDictTypePO::getId);
        Page<SysDictTypePO> result = dictTypeDao.selectPage(pageObj, wrapper);
        List<SysDictTypeAggregate> records = result.getRecords().stream()
                .map(DictTypeInfraConvertor.INSTANCE::toDomain)
                .toList();
        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), records);
    }

    @Override
    public List<SysDictTypeAggregate> findList(DictTypeQuery query) {
        LambdaQueryWrapper<SysDictTypePO> wrapper = new LambdaQueryWrapper<>();
        if (query.getDictName() != null && !query.getDictName().isBlank()) {
            wrapper.like(SysDictTypePO::getDictName, query.getDictName());
        }
        if (query.getDictType() != null && !query.getDictType().isBlank()) {
            wrapper.like(SysDictTypePO::getDictType, query.getDictType());
        }
        if (query.getStatus() != null && !query.getStatus().isBlank()) {
            wrapper.eq(SysDictTypePO::getStatus, query.getStatus());
        }
        wrapper.orderByAsc(SysDictTypePO::getId);
        return dictTypeDao.selectList(wrapper).stream().map(DictTypeInfraConvertor.INSTANCE::toDomain).toList();
    }

    @Override
    public List<SysDictTypeAggregate> findAll() {
        LambdaQueryWrapper<SysDictTypePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(SysDictTypePO::getId);
        return dictTypeDao.selectList(wrapper).stream().map(DictTypeInfraConvertor.INSTANCE::toDomain).toList();
    }

    @Override
    public void save(SysDictTypeAggregate aggregate) {
        SysDictTypePO po = DictTypeInfraConvertor.INSTANCE.toPo(aggregate);
        if (aggregate.getId() != null) {
            dictTypeDao.updateById(po);
        } else {
            dictTypeDao.insert(po);
            aggregate.setId(po.getId());
        }
    }

    @Override
    public void deleteById(Long dictId) {
        dictTypeDao.deleteById(dictId);
    }

    @Override
    public boolean existsByDictType(String dictType, Long excludeId) {
        LambdaQueryWrapper<SysDictTypePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDictTypePO::getDictType, dictType);
        if (excludeId != null) {
            wrapper.ne(SysDictTypePO::getId, excludeId);
        }
        return dictTypeDao.selectCount(wrapper) > 0;
    }
}
