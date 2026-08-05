-- ----------------------------
-- 公告已读记录表
-- ----------------------------
DROP TABLE IF EXISTS sys_notice_read;
CREATE TABLE sys_notice_read
(
    read_id   BIGINT   NOT NULL AUTO_INCREMENT COMMENT '已读主键',
    notice_id BIGINT   NOT NULL COMMENT '公告ID',
    user_id   BIGINT   NOT NULL COMMENT '用户ID',
    read_time DATETIME NOT NULL COMMENT '阅读时间',
    PRIMARY KEY (read_id),
    UNIQUE KEY uk_user_notice (user_id, notice_id) COMMENT '同一用户同一公告只记录一次'
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8mb4 COMMENT = '公告已读记录表';
