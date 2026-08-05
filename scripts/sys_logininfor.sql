-- ----------------------------
-- 系统访问记录表
-- ----------------------------
DROP TABLE IF EXISTS sys_logininfor;
CREATE TABLE sys_logininfor
(
    info_id     BIGINT       NOT NULL AUTO_INCREMENT COMMENT '访问ID',
    user_name   VARCHAR(50)  DEFAULT '' NULL COMMENT '用户账号',
    ipaddr      VARCHAR(128) DEFAULT '' NULL COMMENT '登录IP地址',
    status      TINYINT      DEFAULT 0  NULL COMMENT '登录状态（0成功 1失败）',
    msg         VARCHAR(255) DEFAULT '' NULL COMMENT '提示信息',
    access_time DATETIME                NULL COMMENT '访问时间',
    PRIMARY KEY (info_id),
    KEY idx_sys_logininfor_lt (access_time),
    KEY idx_sys_logininfor_s (status)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8mb4 COMMENT = '系统访问记录';
