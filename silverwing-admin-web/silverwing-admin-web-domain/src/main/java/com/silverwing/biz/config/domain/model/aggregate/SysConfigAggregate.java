package com.silverwing.biz.config.domain.model.aggregate;

import com.silverwing.common.entity.DomainEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 参数配置聚合根，对应数据库表 sys_config。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysConfigAggregate extends DomainEntity {

    /** 参数主键 */
    private Long id;

    /** 参数名称 */
    private String configName;

    /** 参数键名 */
    private String configKey;

    /** 参数键值 */
    private String configValue;

    /** 系统内置（Y-是 N-否） */
    private String configType;

    /** 备注 */
    private String remark;
}
