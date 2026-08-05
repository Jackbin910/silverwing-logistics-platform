package com.silverwing.admin.application.command;

import com.silverwing.admin.application.dto.NoticeResponse;
import com.silverwing.admin.client.NoticeClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 通知公告命令服务。
 */
@Service
@RequiredArgsConstructor
public class NoticeCommandService {

    private final NoticeClient noticeClient;

    /** 新增通知公告 */
    public NoticeResponse create(SaveNoticeCommand command) {
        return noticeClient.create(command);
    }

    /** 修改通知公告 */
    public void update(Long id, SaveNoticeCommand command) {
        noticeClient.update(id, command);
    }

    /** 批量删除通知公告 */
    public void delete(Long[] ids) {
        noticeClient.deleteByIds(ids);
    }

    /** 标记单条公告为指定用户已读 */
    public void markRead(Long noticeId, Long userId) {
        noticeClient.markRead(noticeId, userId);
    }

    /** 批量标记公告为指定用户已读 */
    public void markReadBatch(Long userId, Long[] noticeIds) {
        noticeClient.markReadBatch(userId, noticeIds);
    }
}
