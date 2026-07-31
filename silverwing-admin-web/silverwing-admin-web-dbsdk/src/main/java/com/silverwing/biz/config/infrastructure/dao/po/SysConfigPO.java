package com.silverwing.biz.config.infrastructure.dao.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.silverwing.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 参数配置持久化对象（PO），对应 sys_config 表。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_config")
public class SysConfigPO extends BaseEntity {

    @TableId(type = IdType.AUTO)
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
