package com.silverwing.admin.client.impl;

import com.silverwing.admin.application.command.SaveDictTypeCommand;
import com.silverwing.admin.application.dto.DictTypeResponse;
import com.silverwing.admin.application.query.DictTypePageQuery;
import com.silverwing.admin.client.DictTypeClient;
import com.silverwing.admin.client.convertor.DictTypeConvertor;
import com.silverwing.biz.dict.domain.adapter.repository.DictTypeRepository;
import com.silverwing.biz.dict.domain.model.aggregate.SysDictTypeAggregate;
import com.silverwing.biz.dict.domain.model.query.DictTypeQuery;
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
 * 字典类型上下文防腐层适配器。
 * <p>本类是唯一直接依赖 biz-dict 字典类型领域层（聚合根、仓储、领域服务）的位置。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DictTypeClientImpl implements DictTypeClient {

    private final DictTypeRepository dictTypeRepository;
    private final DictTypeConvertor dictTypeConvertor;
    private final IDictDomainService dictDomainService;
    private final DictCache dictCache;

    @Override
    @Transactional
    public DictTypeResponse create(SaveDictTypeCommand command) {
        SysDictTypeAggregate aggregate = dictTypeConvertor.toEntity(command);
        dictDomainService.saveDictType(aggregate);
        log.info("新建字典类型 dictType={}, id={}", aggregate.getDictType(), aggregate.getId());
        return dictTypeConvertor.toResponse(aggregate);
    }

    @Override
    @Transactional
    public void update(Long id, SaveDictTypeCommand command) {
        SysDictTypeAggregate aggregate = dictTypeRepository.findById(id);
        if (aggregate == null) {
            throw BusinessException.i18n(ResultCode.NOT_FOUND, "admin.dict.type.notfound");
        }
        dictTypeConvertor.applyCommandToEntity(aggregate, command);
        dictDomainService.saveDictType(aggregate);
        log.info("更新字典类型 id={}", id);
    }

    @Override
    @Transactional
    public void deleteByIds(Long[] ids) {
        if (ids == null) {
            return;
        }
        for (Long id : ids) {
            dictDomainService.deleteDictTypeById(id);
        }
        log.info("批量删除字典类型 数量={}", ids.length);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<DictTypeResponse> list(DictTypePageQuery query) {
        DictTypeQuery domainQuery = toDictTypeQuery(query);
        PageResult<SysDictTypeAggregate> page = dictTypeRepository.findPage(domainQuery);
        List<DictTypeResponse> records = page.getRecords().stream()
                .map(dictTypeConvertor::toResponse)
                .collect(Collectors.toList());
        return new PageResult<>(page.getCurrent(), page.getSize(), page.getTotal(), records);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DictTypeResponse> listExport(DictTypePageQuery query) {
        DictTypeQuery domainQuery = toDictTypeQuery(query);
        return dictTypeRepository.findList(domainQuery).stream()
                .map(dictTypeConvertor::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DictTypeResponse getById(Long id) {
        SysDictTypeAggregate aggregate = dictTypeRepository.findById(id);
        return aggregate == null ? null : dictTypeConvertor.toResponse(aggregate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DictTypeResponse> optionSelect() {
        return dictTypeRepository.findAll().stream()
                .map(dictTypeConvertor::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void refreshCache() {
        dictCache.clear();
        log.info("刷新字典缓存完成");
    }

    /** 将应用层分页查询翻译为字典类型领域查询 */
    private DictTypeQuery toDictTypeQuery(DictTypePageQuery query) {
        DictTypeQuery domainQuery = new DictTypeQuery();
        domainQuery.setCurrent(query.getCurrent());
        domainQuery.setSize(query.getSize());
        domainQuery.setDictName(query.getDictName());
        domainQuery.setDictType(query.getDictType());
        domainQuery.setStatus(query.getStatus());
        return domainQuery;
    }
}
