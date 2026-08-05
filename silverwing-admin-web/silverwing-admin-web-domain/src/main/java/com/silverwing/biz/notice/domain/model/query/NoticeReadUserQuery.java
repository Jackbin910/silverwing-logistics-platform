package com.silverwing.biz.notice.domain.model.query;

import com.silverwing.common.domain.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 公告已读用户查询条件（含分页参数）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NoticeReadUserQuery extends PageRequest {

    /** 公告ID */
    private Long noticeId;

    /** 账号或昵称模糊搜索值 */
    private String searchValue;
}
