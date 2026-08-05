package com.silverwing.biz.notice.domain.service;

import com.silverwing.biz.notice.domain.model.aggregate.SysNoticeAggregate;

/**
 * 通知公告领域服务，负责公告的业务校验及持久化。
 */
public interface INoticeDomainService {

    /** 保存通知公告（含标题唯一性校验） */
    void saveNotice(SysNoticeAggregate aggregate);

    /** 删除通知公告，并级联清理其已读记录 */
    void deleteNoticeById(Long noticeId);

    /** 标记单条公告为当前用户已读 */
    void markRead(Long noticeId, Long userId);

    /** 批量标记公告为当前用户已读 */
    void markReadBatch(Long userId, Long[] noticeIds);
}
