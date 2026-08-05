package com.silverwing.biz.notice.infrastructure.adapter.repository;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.silverwing.biz.notice.domain.adapter.repository.NoticeReadRepository;
import com.silverwing.biz.notice.domain.model.entity.NoticeReadUserEntity;
import com.silverwing.biz.notice.domain.model.query.NoticeReadUserQuery;
import com.silverwing.biz.notice.infrastructure.adapter.repository.convertor.NoticeReadInfraConvertor;
import com.silverwing.biz.notice.infrastructure.dao.SysNoticeReadDao;
import com.silverwing.biz.notice.infrastructure.dao.po.NoticeReadUserPO;
import com.silverwing.common.domain.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 公告已读记录仓储实现（基础设施适配器）。
 */
@Repository
@RequiredArgsConstructor
public class NoticeReadRepositoryImpl implements NoticeReadRepository {

    private final SysNoticeReadDao noticeReadDao;

    @Override
    public void markRead(Long noticeId, Long userId) {
        noticeReadDao.insertNoticeRead(noticeId, userId);
    }

    @Override
    public void markReadBatch(Long userId, Long[] noticeIds) {
        if (noticeIds == null || noticeIds.length == 0) {
            return;
        }
        noticeReadDao.insertNoticeReadBatch(userId, noticeIds);
    }

    @Override
    public PageResult<NoticeReadUserEntity> findReadUsers(NoticeReadUserQuery query) {
        query.normalize();
        Page<NoticeReadUserPO> page = new Page<>(query.getCurrent(), query.getSize());
        IPage<NoticeReadUserPO> result = noticeReadDao.selectReadUsersByNoticeId(
                page, query.getNoticeId(), query.getSearchValue());
        List<NoticeReadUserEntity> records = result.getRecords().stream()
                .map(NoticeReadInfraConvertor.INSTANCE::toDomain)
                .toList();
        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), records);
    }

    @Override
    public void deleteByNoticeIds(Long[] noticeIds) {
        if (noticeIds == null || noticeIds.length == 0) {
            return;
        }
        noticeReadDao.deleteByNoticeIds(noticeIds);
    }
}
