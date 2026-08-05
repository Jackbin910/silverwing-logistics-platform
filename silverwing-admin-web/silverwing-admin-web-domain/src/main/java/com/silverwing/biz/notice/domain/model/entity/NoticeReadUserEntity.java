package com.silverwing.biz.notice.domain.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 公告已读用户领域模型。
 */
@Data
public class NoticeReadUserEntity {

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
