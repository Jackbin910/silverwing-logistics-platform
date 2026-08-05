package com.silverwing.biz.notice.domain.service.impl;

import com.silverwing.biz.notice.domain.adapter.repository.NoticeReadRepository;
import com.silverwing.biz.notice.domain.adapter.repository.NoticeRepository;
import com.silverwing.biz.notice.domain.model.aggregate.SysNoticeAggregate;
import com.silverwing.biz.notice.domain.service.INoticeDomainService;
import com.silverwing.common.domain.ResultCode;
import com.silverwing.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 通知公告领域服务实现，负责公告标题唯一性校验及持久化。
 */
@Service
@RequiredArgsConstructor
public class NoticeDomainServiceImpl implements INoticeDomainService {

    private final NoticeRepository noticeRepository;
    private final NoticeReadRepository noticeReadRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveNotice(SysNoticeAggregate aggregate) {
        // 同标题公告不允许重复创建，避免用户误操作产生重复通知
        if (noticeRepository.existsByNoticeTitle(aggregate.getNoticeTitle(), aggregate.getId())) {
            throw new BusinessException(ResultCode.DATA_ALREADY_EXISTS, "admin.notice.title.exists",
                    aggregate.getNoticeTitle());
        }
        noticeRepository.save(aggregate);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteNoticeById(Long noticeId) {
        noticeRepository.deleteById(noticeId);
        // 公告删除后其已读记录已无意义，需级联清理避免脏数据堆积
        noticeReadRepository.deleteByNoticeIds(new Long[]{noticeId});
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markRead(Long noticeId, Long userId) {
        noticeReadRepository.markRead(noticeId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markReadBatch(Long userId, Long[] noticeIds) {
        if (noticeIds == null || noticeIds.length == 0) {
            return;
        }
        noticeReadRepository.markReadBatch(userId, noticeIds);
    }
}
