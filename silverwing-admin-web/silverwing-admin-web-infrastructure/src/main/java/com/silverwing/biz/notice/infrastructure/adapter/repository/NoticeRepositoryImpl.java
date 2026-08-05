package com.silverwing.biz.notice.infrastructure.adapter.repository;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.silverwing.biz.notice.domain.adapter.repository.NoticeRepository;
import com.silverwing.biz.notice.domain.model.aggregate.SysNoticeAggregate;
import com.silverwing.biz.notice.domain.model.query.NoticeQuery;
import com.silverwing.biz.notice.infrastructure.adapter.repository.convertor.NoticeInfraConvertor;
import com.silverwing.biz.notice.infrastructure.dao.SysNoticeDao;
import com.silverwing.biz.notice.infrastructure.dao.po.SysNoticePO;
import com.silverwing.common.domain.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 通知公告仓储实现（基础设施适配器）。
 */
@Repository
@RequiredArgsConstructor
public class NoticeRepositoryImpl implements NoticeRepository {

    private final SysNoticeDao noticeDao;

    @Override
    public SysNoticeAggregate findById(Long noticeId) {
        SysNoticePO po = noticeDao.selectById(noticeId);
        return po == null ? null : NoticeInfraConvertor.INSTANCE.toDomain(po);
    }

    @Override
    public PageResult<SysNoticeAggregate> findPage(NoticeQuery query) {
        query.normalize();
        Page<SysNoticePO> pageObj = new Page<>(query.getCurrent(), query.getSize());
        Page<SysNoticePO> result = noticeDao.selectPage(pageObj, buildWrapper(query));
        List<SysNoticeAggregate> records = result.getRecords().stream()
                .map(NoticeInfraConvertor.INSTANCE::toDomain)
                .toList();
        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), records);
    }

    @Override
    public List<SysNoticeAggregate> findList(NoticeQuery query) {
        return noticeDao.selectList(buildWrapper(query)).stream()
                .map(NoticeInfraConvertor.INSTANCE::toDomain)
                .toList();
    }

    @Override
    public void save(SysNoticeAggregate aggregate) {
        SysNoticePO po = NoticeInfraConvertor.INSTANCE.toPo(aggregate);
        if (aggregate.getId() != null) {
            noticeDao.updateById(po);
        } else {
            noticeDao.insert(po);
            aggregate.setId(po.getId());
        }
    }

    @Override
    public void deleteById(Long noticeId) {
        noticeDao.deleteById(noticeId);
    }

    @Override
    public boolean existsByNoticeTitle(String noticeTitle, Long excludeId) {
        LambdaQueryWrapper<SysNoticePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysNoticePO::getNoticeTitle, noticeTitle);
        if (excludeId != null) {
            wrapper.ne(SysNoticePO::getId, excludeId);
        }
        return noticeDao.selectCount(wrapper) > 0;
    }

    /** 构建通知公告查询条件（简单条件统一使用 Wrapper 拼装） */
    private LambdaQueryWrapper<SysNoticePO> buildWrapper(NoticeQuery query) {
        LambdaQueryWrapper<SysNoticePO> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(query.getNoticeTitle())) {
            wrapper.like(SysNoticePO::getNoticeTitle, query.getNoticeTitle());
        }
        if (StrUtil.isNotBlank(query.getNoticeType())) {
            wrapper.eq(SysNoticePO::getNoticeType, query.getNoticeType());
        }
        if (StrUtil.isNotBlank(query.getCreateBy())) {
            wrapper.like(SysNoticePO::getCreateBy, query.getCreateBy());
        }
        if (query.getStatus() != null) {
            wrapper.eq(SysNoticePO::getStatus, query.getStatus());
        }
        wrapper.orderByDesc(SysNoticePO::getId);
        return wrapper;
    }
}
