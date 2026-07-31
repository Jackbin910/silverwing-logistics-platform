package com.silverwing.admin.client.impl;

import com.silverwing.admin.application.command.SaveDictDataCommand;
import com.silverwing.admin.application.dto.DictDataResponse;
import com.silverwing.admin.application.query.DictDataPageQuery;
import com.silverwing.admin.client.DictDataClient;
import com.silverwing.admin.client.convertor.DictDataConvertor;
import com.silverwing.biz.dict.domain.adapter.repository.DictDataRepository;
import com.silverwing.biz.dict.domain.model.aggregate.SysDictDataAggregate;
import com.silverwing.biz.dict.domain.model.query.DictDataQuery;
import com.silverwing.biz.dict.domain.service.IDictDomainService;
import com.silverwing.biz.dict.infrastructure.cache.DictCache;
import com.silverwing.common.domain.PageResult;
import com.silverwing.common.domain.ResultCode;
import com.silverwing.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 字典数据上下文防腐层适配器。
 * <p>本类是唯一直接依赖 biz-dict 字典数据领域层（聚合根、仓储、领域服务）的位置。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DictDataClientImpl implements DictDataClient {

    private final DictDataRepository dictDataRepository;
    private final DictDataConvertor dictDataConvertor;
    private final IDictDomainService dictDomainService;
    private final DictCache dictCache;

    @Override
    @Transactional
    public DictDataResponse create(SaveDictDataCommand command) {
        SysDictDataAggregate aggregate = dictDataConvertor.toEntity(command);
        dictDomainService.saveDictData(aggregate);
        log.info("新建字典数据 dictType={}, dictLabel={}, id={}",
                aggregate.getDictType(), aggregate.getDictLabel(), aggregate.getId());
        return dictDataConvertor.toResponse(aggregate);
    }

    @Override
    @Transactional
    public void update(Long id, SaveDictDataCommand command) {
        SysDictDataAggregate aggregate = dictDataRepository.findById(id);
        if (aggregate == null) {
            throw BusinessException.i18n(ResultCode.NOT_FOUND, "admin.dict.data.notfound");
        }
        dictDataConvertor.applyCommandToEntity(aggregate, command);
        dictDomainService.saveDictData(aggregate);
        log.info("更新字典数据 id={}", id);
    }

    @Override
    @Transactional
    public void deleteByIds(Long[] ids) {
        if (ids == null) {
            return;
        }
        for (Long id : ids) {
            dictDomainService.deleteDictDataById(id);
        }
        log.info("批量删除字典数据 数量={}", ids.length);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<DictDataResponse> list(DictDataPageQuery query) {
        DictDataQuery domainQuery = toDictDataQuery(query);
        PageResult<SysDictDataAggregate> page = dictDataRepository.findPage(domainQuery);
        List<DictDataResponse> records = page.getRecords().stream()
                .map(dictDataConvertor::toResponse)
                .collect(Collectors.toList());
        return new PageResult<>(page.getCurrent(), page.getSize(), page.getTotal(), records);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DictDataResponse> listExport(DictDataPageQuery query) {
        DictDataQuery domainQuery = toDictDataQuery(query);
        return dictDataRepository.findList(domainQuery).stream()
                .map(dictDataConvertor::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DictDataResponse getById(Long id) {
        SysDictDataAggregate aggregate = dictDataRepository.findById(id);
        return aggregate == null ? null : dictDataConvertor.toResponse(aggregate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DictDataResponse> getByDictType(String dictType) {
        List<SysDictDataAggregate> cached = dictCache.get(dictType);
        if (cached == null) {
            cached = dictDataRepository.findByDictType(dictType);
            dictCache.put(dictType, cached);
        }
        return cached.stream().map(dictDataConvertor::toResponse).collect(Collectors.toList());
    }

    /** 将应用层分页查询翻译为字典数据领域查询 */
    private DictDataQuery toDictDataQuery(DictDataPageQuery query) {
        DictDataQuery domainQuery = new DictDataQuery();
        domainQuery.setCurrent(query.getCurrent());
        domainQuery.setSize(query.getSize());
        domainQuery.setDictLabel(query.getDictLabel());
        domainQuery.setDictValue(query.getDictValue());
        domainQuery.setDictType(query.getDictType());
        domainQuery.setStatus(query.getStatus());
        return domainQuery;
    }
}
