package com.silverwing.biz.config.domain.model.query;

import com.silverwing.common.domain.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 参数配置查询条件。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ConfigQuery extends PageRequest {

    /** 参数名称（模糊） */
    private String configName;

    /** 参数键名（模糊） */
    private String configKey;

    /** 系统内置（Y-是 N-否） */
    private String configType;
}
