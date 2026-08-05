package com.silverwing.biz.notice.infrastructure.dao.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 公告已读记录持久化对象（PO），对应 sys_notice_read 表。
 * <p>该表仅记录阅读行为，不含创建人/更新人等审计字段，故不继承 BaseEntity。</p>
 */
@Data
@TableName(value = "sys_notice_read")
public class SysNoticeReadPO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 已读主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 公告ID */
    private Long noticeId;

    /** 用户ID */
    private Long userId;

    /** 阅读时间 */
    private LocalDateTime readTime;
}
