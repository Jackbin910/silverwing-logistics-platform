package com.silverwing.admin.application.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 保存字典数据命令。
 */
@Data
@Schema(description = "保存字典数据命令")
public class SaveDictDataCommand {

    @Schema(description = "字典数据主键，更新时必填")
    private Long id;

    @Schema(description = "字典排序", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{admin.dict.data.sort.required}")
    private Long dictSort;

    @Schema(description = "字典标签", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{admin.dict.data.label.required}")
    private String dictLabel;

    @Schema(description = "字典键值", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{admin.dict.data.value.required}")
    private String dictValue;

    @Schema(description = "字典类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{admin.dict.data.type.required}")
    private String dictType;

    @Schema(description = "样式属性（其他样式扩展）")
    private String cssClass;

    @Schema(description = "表格字典样式")
    private String listClass;

    @Schema(description = "是否默认（Y-是 N-否）")
    private String isDefault;

    @Schema(description = "状态（0-正常 1-停用）")
    private String status;

    @Schema(description = "备注")
    private String remark;
}
