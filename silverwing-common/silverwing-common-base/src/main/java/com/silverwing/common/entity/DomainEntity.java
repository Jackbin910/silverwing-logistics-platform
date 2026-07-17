package com.silverwing.common.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 领域基类（纯领域对象，不含任何持久化注解）
 * <p>
 * 抽取领域实体/聚合根共有的审计字段，与基础设施层的 {@link BaseEntity}（带 MyBatis-Plus 注解）
 * </p>
 */
@Data
public class DomainEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 创建人 */
    private String createBy;

    /** 更新人 */
    private String updateBy;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 逻辑删除标记（0-未删除，1-已删除） */
    private Integer deleted;
}
