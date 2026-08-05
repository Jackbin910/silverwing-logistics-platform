package com.silverwing.admin.client.impl;

import com.silverwing.admin.application.command.SaveNoticeCommand;
import com.silverwing.admin.application.dto.NoticeReadUserResponse;
import com.silverwing.admin.application.dto.NoticeResponse;
import com.silverwing.admin.application.query.NoticePageQuery;
import com.silverwing.admin.application.query.NoticeReadUserPageQuery;
import com.silverwing.admin.client.NoticeClient;
import com.silverwing.admin.client.convertor.NoticeConvertor;
import com.silverwing.biz.notice.domain.adapter.repository.NoticeReadRepository;
import com.silverwing.biz.notice.domain.adapter.repository.NoticeRepository;
import com.silverwing.biz.notice.domain.model.aggregate.SysNoticeAggregate;
import com.silverwing.biz.notice.domain.model.entity.NoticeReadUserEntity;
import com.silverwing.biz.notice.domain.model.query.NoticeQuery;
import com.silverwing.biz.notice.domain.model.query.NoticeReadUserQuery;
import com.silverwing.biz.notice.domain.service.INoticeDomainService;
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
 * 通知公告上下文防腐层适配器。
 * <p>本类是唯一直接依赖 biz-notice 公告领域层（聚合根、仓储、领域服务）的位置。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NoticeClientImpl implements NoticeClient {

    private final NoticeRepository noticeRepository;
    private final NoticeReadRepository noticeReadRepository;
    private final NoticeConvertor noticeConvertor;
    private final INoticeDomainService noticeDomainService;

    @Override
    @Transactional
    public NoticeResponse create(SaveNoticeCommand command) {
        SysNoticeAggregate aggregate = noticeConvertor.toEntity(command);
        noticeDomainService.saveNotice(aggregate);
        log.info("新建通知公告 title={}, id={}", aggregate.getNoticeTitle(), aggregate.getId());
        return noticeConvertor.toResponse(aggregate);
    }

    @Override
    @Transactional
    public void update(Long id, SaveNoticeCommand command) {
        SysNoticeAggregate aggregate = noticeRepository.findById(id);
        if (aggregate == null) {
            throw BusinessException.i18n(ResultCode.NOT_FOUND, "admin.notice.notfound");
        }
        noticeConvertor.applyCommandToEntity(aggregate, command);
        noticeDomainService.saveNotice(aggregate);
        log.info("更新通知公告 id={}", id);
    }

    @Override
    @Transactional
    public void deleteByIds(Long[] ids) {
        if (ids == null) {
            return;
        }
        for (Long id : ids) {
            noticeDomainService.deleteNoticeById(id);
        }
        log.info("批量删除通知公告 数量={}", ids.length);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<NoticeResponse> list(NoticePageQuery query) {
        NoticeQuery domainQuery = toNoticeQuery(query);
        PageResult<SysNoticeAggregate> page = noticeRepository.findPage(domainQuery);
        List<NoticeResponse> records = page.getRecords().stream()
                .map(noticeConvertor::toResponse)
                .collect(Collectors.toList());
        return new PageResult<>(page.getCurrent(), page.getSize(), page.getTotal(), records);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NoticeResponse> listExport(NoticePageQuery query) {
        NoticeQuery domainQuery = toNoticeQuery(query);
        return noticeRepository.findList(domainQuery).stream()
                .map(noticeConvertor::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public NoticeResponse getById(Long id) {
        SysNoticeAggregate aggregate = noticeRepository.findById(id);
        return aggregate == null ? null : noticeConvertor.toResponse(aggregate);
    }

    @Override
    @Transactional
    public void markRead(Long noticeId, Long userId) {
        noticeDomainService.markRead(noticeId, userId);
    }

    @Override
    @Transactional
    public void markReadBatch(Long userId, Long[] noticeIds) {
        noticeDomainService.markReadBatch(userId, noticeIds);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<NoticeReadUserResponse> readUsers(NoticeReadUserPageQuery query) {
        NoticeReadUserQuery domainQuery = new NoticeReadUserQuery();
        domainQuery.setCurrent(query.getCurrent());
        domainQuery.setSize(query.getSize());
        domainQuery.setNoticeId(query.getNoticeId());
        domainQuery.setSearchValue(query.getSearchValue());
        PageResult<NoticeReadUserEntity> page = noticeReadRepository.findReadUsers(domainQuery);
        List<NoticeReadUserResponse> records = page.getRecords().stream()
                .map(noticeConvertor::toReadUserResponse)
                .collect(Collectors.toList());
        return new PageResult<>(page.getCurrent(), page.getSize(), page.getTotal(), records);
    }

    /** 将应用层分页查询翻译为公告领域查询 */
    private NoticeQuery toNoticeQuery(NoticePageQuery query) {
        NoticeQuery domainQuery = new NoticeQuery();
        domainQuery.setCurrent(query.getCurrent());
        domainQuery.setSize(query.getSize());
        domainQuery.setNoticeTitle(query.getNoticeTitle());
        domainQuery.setNoticeType(query.getNoticeType());
        domainQuery.setCreateBy(query.getCreateBy());
        domainQuery.setStatus(query.getStatus());
        return domainQuery;
    }
}
