package com.silverwing.biz.notice.infrastructure.dao.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.silverwing.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 通知公告持久化对象（PO），对应 sys_notice 表。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_notice")
public class SysNoticePO extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 公告标题 */
    private String noticeTitle;

    /** 公告类型（1-通知 2-公告） */
    private String noticeType;

    /** 公告内容（数据库为 longblob，使用字节数组承载） */
    private byte[] noticeContent;

    /** 公告状态（0-正常 1-关闭） */
    private Integer status;

    /** 备注 */
    private String remark;

    /** 当前用户是否已读，非表字段，由关联查询填充 */
    @TableField(exist = false)
    private Boolean isRead;
}
