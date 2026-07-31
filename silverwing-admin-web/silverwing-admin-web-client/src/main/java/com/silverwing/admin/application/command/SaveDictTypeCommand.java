package com.silverwing.admin.application.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 保存字典类型命令。
 */
@Data
@Schema(description = "保存字典类型命令")
public class SaveDictTypeCommand {

    @Schema(description = "字典主键，更新时必填")
    private Long id;

    @Schema(description = "字典名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{admin.dict.type.name.required}")
    private String dictName;

    @Schema(description = "字典类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{admin.dict.type.code.required}")
    private String dictType;

    @Schema(description = "状态（0-正常 1-停用）")
    private String status;

    @Schema(description = "备注")
    private String remark;
}
