package com.silverwing.biz.notice.domain.adapter.repository;

import com.silverwing.biz.notice.domain.model.aggregate.SysNoticeAggregate;
import com.silverwing.biz.notice.domain.model.query.NoticeQuery;
import com.silverwing.common.domain.PageResult;

import java.util.List;

/**
 * 通知公告仓储接口。
 */
public interface NoticeRepository {

    /** 分页查询通知公告 */
    PageResult<SysNoticeAggregate> findPage(NoticeQuery query);

    /** 条件查询全部通知公告（用于导出） */
    List<SysNoticeAggregate> findList(NoticeQuery query);

    /** 根据主键查询通知公告 */
    SysNoticeAggregate findById(Long noticeId);

    /** 保存（新增或更新）通知公告 */
    void save(SysNoticeAggregate aggregate);

    /** 根据主键删除通知公告 */
    void deleteById(Long noticeId);

    /** 判断同标题公告是否已存在（排除指定主键） */
    boolean existsByNoticeTitle(String noticeTitle, Long excludeId);
}
