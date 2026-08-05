package com.silverwing.biz.notice.domain.model.query;

import com.silverwing.common.domain.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 通知公告查询条件（含分页参数）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NoticeQuery extends PageRequest {

    /** 公告标题（模糊匹配） */
    private String noticeTitle;

    /** 公告类型（1-通知 2-公告） */
    private String noticeType;

    /** 创建者（模糊匹配） */
    private String createBy;

    /** 公告状态（0-正常 1-关闭） */
    private Integer status;
}
