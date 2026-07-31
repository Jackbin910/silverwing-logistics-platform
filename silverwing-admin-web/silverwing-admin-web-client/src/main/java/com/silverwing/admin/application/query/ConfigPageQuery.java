package com.silverwing.admin.application.query;

import com.silverwing.common.domain.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 参数配置分页查询。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "参数配置分页查询")
public class ConfigPageQuery extends PageRequest {

    @Schema(description = "参数名称")
    private String configName;

    @Schema(description = "参数键名")
    private String configKey;

    @Schema(description = "系统内置（Y-是 N-否）")
    private String configType;
}
