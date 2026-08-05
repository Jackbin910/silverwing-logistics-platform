package com.silverwing.admin.client;

import com.silverwing.admin.application.command.SaveNoticeCommand;
import com.silverwing.admin.application.dto.NoticeReadUserResponse;
import com.silverwing.admin.application.dto.NoticeResponse;
import com.silverwing.admin.application.query.NoticePageQuery;
import com.silverwing.admin.application.query.NoticeReadUserPageQuery;
import com.silverwing.common.domain.PageResult;

import java.util.List;

/**
 * 通知公告上下文防腐层端口。
 * <p>应用层通过该端口访问 biz-notice 公告上下文，隔离对聚合根、仓储与领域服务的直接依赖。</p>
 */
public interface NoticeClient {

    /** 创建通知公告 */
    NoticeResponse create(SaveNoticeCommand command);

    /** 更新通知公告 */
    void update(Long id, SaveNoticeCommand command);

    /** 批量删除通知公告 */
    void deleteByIds(Long[] ids);

    /** 分页查询通知公告 */
    PageResult<NoticeResponse> list(NoticePageQuery query);

    /** 查询全部通知公告（用于导出） */
    List<NoticeResponse> listExport(NoticePageQuery query);

    /** 根据主键查询通知公告 */
    NoticeResponse getById(Long id);

    /** 标记单条公告为指定用户已读 */
    void markRead(Long noticeId, Long userId);

    /** 批量标记公告为指定用户已读 */
    void markReadBatch(Long userId, Long[] noticeIds);

    /** 分页查询已阅读指定公告的用户列表 */
    PageResult<NoticeReadUserResponse> readUsers(NoticeReadUserPageQuery query);
}
