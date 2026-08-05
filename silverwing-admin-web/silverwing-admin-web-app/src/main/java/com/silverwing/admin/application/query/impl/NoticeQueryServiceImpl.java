package com.silverwing.admin.application.query.impl;

import com.silverwing.admin.application.dto.NoticeReadUserResponse;
import com.silverwing.admin.application.dto.NoticeResponse;
import com.silverwing.admin.application.query.NoticePageQuery;
import com.silverwing.admin.application.query.NoticeQueryService;
import com.silverwing.admin.application.query.NoticeReadUserPageQuery;
import com.silverwing.admin.client.NoticeClient;
import com.silverwing.common.domain.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 通知公告查询服务实现。
 */
@Service
@RequiredArgsConstructor
public class NoticeQueryServiceImpl implements NoticeQueryService {

    private final NoticeClient noticeClient;

    @Override
    public PageResult<NoticeResponse> list(NoticePageQuery query) {
        return noticeClient.list(query);
    }

    @Override
    public List<NoticeResponse> listExport(NoticePageQuery query) {
        return noticeClient.listExport(query);
    }

    @Override
    public NoticeResponse getById(Long id) {
        return noticeClient.getById(id);
    }

    @Override
    public PageResult<NoticeReadUserResponse> readUsers(NoticeReadUserPageQuery query) {
        return noticeClient.readUsers(query);
    }
}
