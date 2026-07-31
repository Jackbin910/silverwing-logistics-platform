package com.silverwing.biz.dict.domain.service.impl;

import com.silverwing.biz.dict.domain.adapter.repository.DictDataRepository;
import com.silverwing.biz.dict.domain.adapter.repository.DictTypeRepository;
import com.silverwing.biz.dict.domain.model.aggregate.SysDictDataAggregate;
import com.silverwing.biz.dict.domain.model.aggregate.SysDictTypeAggregate;
import com.silverwing.biz.dict.domain.service.IDictDomainService;
import com.silverwing.common.domain.ResultCode;
import com.silverwing.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 字典领域服务实现，负责字典类型与字典数据的唯一性校验及持久化。
 */
@Service
@RequiredArgsConstructor
public class DictDomainServiceImpl implements IDictDomainService {

    private final DictTypeRepository dictTypeRepository;
    private final DictDataRepository dictDataRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveDictType(SysDictTypeAggregate aggregate) {
        if (dictTypeRepository.existsByDictType(aggregate.getDictType(), aggregate.getId())) {
            throw new BusinessException(ResultCode.DATA_ALREADY_EXISTS, "admin.dict.type.exists",
                    aggregate.getDictType());
        }
        dictTypeRepository.save(aggregate);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDictTypeById(Long dictId) {
        dictTypeRepository.deleteById(dictId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveDictData(SysDictDataAggregate aggregate) {
        if (dictDataRepository.existsByDictTypeAndValue(aggregate.getDictType(), aggregate.getDictValue(),
                aggregate.getId())) {
            throw new BusinessException(ResultCode.DATA_ALREADY_EXISTS, "admin.dict.data.value.exists",
                    aggregate.getDictValue());
        }
        dictDataRepository.save(aggregate);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDictDataById(Long dictCode) {
        dictDataRepository.deleteById(dictCode);
    }
}
