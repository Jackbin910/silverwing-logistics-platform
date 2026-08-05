package com.silverwing.biz.notice.domain.adapter.repository;

import com.silverwing.biz.notice.domain.model.entity.NoticeReadUserEntity;
import com.silverwing.biz.notice.domain.model.query.NoticeReadUserQuery;
import com.silverwing.common.domain.PageResult;

/**
 * 公告已读记录仓储接口。
 */
public interface NoticeReadRepository {

    /** 标记单条公告已读（幂等） */
    void markRead(Long noticeId, Long userId);

    /** 批量标记公告已读（幂等） */
    void markReadBatch(Long userId, Long[] noticeIds);

    /** 分页查询已阅读指定公告的用户列表 */
    PageResult<NoticeReadUserEntity> findReadUsers(NoticeReadUserQuery query);

    /** 删除公告时清理其已读记录 */
    void deleteByNoticeIds(Long[] noticeIds);
}
