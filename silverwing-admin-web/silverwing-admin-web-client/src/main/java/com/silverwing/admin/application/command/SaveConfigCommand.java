package com.silverwing.admin.application.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 保存参数配置命令。
 */
@Data
@Schema(description = "保存参数配置命令")
public class SaveConfigCommand {

    @Schema(description = "参数主键，更新时必填")
    private Long id;

    @Schema(description = "参数名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{admin.config.name.required}")
    private String configName;

    @Schema(description = "参数键名", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{admin.config.key.required}")
    private String configKey;

    @Schema(description = "参数键值", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{admin.config.value.required}")
    private String configValue;

    @Schema(description = "系统内置（Y-是 N-否）")
    private String configType;

    @Schema(description = "备注")
    private String remark;
}
