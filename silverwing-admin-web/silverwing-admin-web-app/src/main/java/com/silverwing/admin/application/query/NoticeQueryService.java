package com.silverwing.admin.application.query;

import com.silverwing.admin.application.dto.NoticeReadUserResponse;
import com.silverwing.admin.application.dto.NoticeResponse;
import com.silverwing.common.domain.PageResult;

import java.util.List;

/**
 * 通知公告查询服务。
 */
public interface NoticeQueryService {

    /** 分页查询 */
    PageResult<NoticeResponse> list(NoticePageQuery query);

    /** 查询全部（用于导出） */
    List<NoticeResponse> listExport(NoticePageQuery query);

    /** 根据主键查询 */
    NoticeResponse getById(Long id);

    /** 分页查询已阅读指定公告的用户列表 */
    PageResult<NoticeReadUserResponse> readUsers(NoticeReadUserPageQuery query);
}
