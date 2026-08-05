package com.silverwing.biz.notice.domain.model.aggregate;

import com.silverwing.common.entity.DomainEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 通知公告聚合根，对应数据库表 sys_notice。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysNoticeAggregate extends DomainEntity {

    /** 公告ID */
    private Long id;

    /** 公告标题 */
    private String noticeTitle;

    /** 公告类型（1-通知 2-公告） */
    private String noticeType;

    /** 公告内容 */
    private String noticeContent;

    /** 公告状态（0-正常 1-关闭） */
    private Integer status;

    /** 备注 */
    private String remark;
}
