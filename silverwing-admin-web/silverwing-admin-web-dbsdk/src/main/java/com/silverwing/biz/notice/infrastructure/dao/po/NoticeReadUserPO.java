package com.silverwing.biz.notice.infrastructure.dao.po;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 公告已读用户查询结果对象。
 * <p>由 sys_notice_read 关联 sys_user、sys_dept 查询得到，非单表映射。</p>
 */
@Data
public class NoticeReadUserPO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户ID */
    private Long userId;

    /** 用户账号 */
    private String username;

    /** 用户昵称 */
    private String nickname;

    /** 部门名称 */
    private String deptName;

    /** 手机号码 */
    private String phone;

    /** 阅读时间 */
    private LocalDateTime readTime;
}
